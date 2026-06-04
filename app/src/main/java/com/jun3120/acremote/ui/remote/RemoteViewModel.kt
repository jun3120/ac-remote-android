package com.jun3120.acremote.ui.remote

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jun3120.acremote.App
import com.jun3120.acremote.data.ir.IrTransmitter
import net.irext.decode.sdk.IRDecode
import net.irext.decode.sdk.bean.ACStatus
import net.irext.decode.sdk.utils.Constants

class RemoteViewModel : ViewModel() {

    private val irDecode = IRDecode.getInstance()

    // UI 状态（用户看到的空调当前状态）
    private val _powerOn = MutableLiveData(false)
    val powerOn: LiveData<Boolean> = _powerOn

    private val _temperature = MutableLiveData(24)
    val temperature: LiveData<Int> = _temperature

    private val _mode = MutableLiveData(Constants.ACMode.MODE_COOL.value)
    val mode: LiveData<Int> = _mode

    private val _fanSpeed = MutableLiveData(Constants.ACWindSpeed.SPEED_AUTO.value)
    val fanSpeed: LiveData<Int> = _fanSpeed

    private val _swing = MutableLiveData(true)
    val swing: LiveData<Boolean> = _swing

    private val _tempMin = MutableLiveData(16)
    val tempMin: LiveData<Int> = _tempMin
    private val _tempMax = MutableLiveData(30)
    val tempMax: LiveData<Int> = _tempMax

    private val _supportedModes = MutableLiveData(intArrayOf(1, 1, 1, 1, 1))
    val supportedModes: LiveData<IntArray> = _supportedModes

    private val _supportedWindSpeeds = MutableLiveData(intArrayOf(1, 1, 1, 1))
    val supportedWindSpeeds: LiveData<IntArray> = _supportedWindSpeeds

    private val _brandName = MutableLiveData("")
    val brandName: LiveData<String> = _brandName

    private val _errorEvent = MutableLiveData<String?>()
    val errorEvent: LiveData<String?> = _errorEvent

    private var categoryId = 0
    private var initialized = false

    fun init(codePath: String, categoryId: Int, subCategory: Int, brandName: String) {
        if (initialized) return
        this.categoryId = categoryId
        _brandName.value = brandName

        val file = java.io.File(codePath)
        Log.d(TAG, "Opening IR file: $codePath, exists=${file.exists()}, " +
                "size=${file.length()}, category=$categoryId, subCategory=$subCategory")

        // 先关闭可能残留的旧会话
        irDecode.closeBinary()

        val result = irDecode.openFile(categoryId, subCategory, codePath)
        if (result != 0) {
            Log.e(TAG, "Failed to open IR code file: $codePath, error=$result")
            _errorEvent.value = "红外码库加载失败 (error=$result)"
            return
        }

        _supportedModes.value = irDecode.acSupportedMode
        val tempRange = irDecode.getTemperatureRange(Constants.ACMode.MODE_COOL.value)
        _tempMin.value = tempRange.tempMin + 16
        _tempMax.value = tempRange.tempMax + 16
        _supportedWindSpeeds.value = irDecode.getACSupportedWindSpeed(Constants.ACMode.MODE_COOL.value)

        initialized = true
        Log.d(TAG, "IR code loaded successfully")
    }

    // ===== 用户操作 =====
    //
    // 关键设计（对齐 IRext 示例的 ControlHelper）：
    // 每次发指令都构造全新 ACStatus 并设默认值，通过 keyCode 表达本次操作。
    // ACStatus 代表解码基准状态，keyCode 告诉解码库本次要变更哪个维度。

    fun togglePower() {
        val newPower = !(_powerOn.value ?: false)
        val acStatus = freshStatus().apply {
            acPower = if (newPower)
                Constants.ACPower.POWER_ON.value
            else
                Constants.ACPower.POWER_OFF.value
        }
        if (send(Constants.ACFunction.FUNCTION_SWITCH_POWER.value, acStatus)) {
            _powerOn.value = newPower
        }
    }

    fun setMode(mode: Int) {
        val acStatus = freshStatus().apply { acMode = mode }
        if (send(Constants.ACFunction.FUNCTION_CHANGE_MODE.value, acStatus)) {
            _mode.value = mode
        }
    }

    fun tempUp() {
        val current = _temperature.value ?: 24
        val max = _tempMax.value ?: 30
        if (current >= max) return
        val newTemp = current + 1
        val acStatus = freshStatus().apply { acTemp = newTemp - 16 }
        if (send(Constants.ACFunction.FUNCTION_TEMPERATURE_UP.value, acStatus)) {
            _temperature.value = newTemp
        }
    }

    fun tempDown() {
        val current = _temperature.value ?: 24
        val min = _tempMin.value ?: 16
        if (current <= min) return
        val newTemp = current - 1
        val acStatus = freshStatus().apply { acTemp = newTemp - 16 }
        if (send(Constants.ACFunction.FUNCTION_TEMPERATURE_DOWN.value, acStatus)) {
            _temperature.value = newTemp
        }
    }

    fun setFanSpeed(speed: Int) {
        val acStatus = freshStatus().apply { acWindSpeed = speed }
        if (send(Constants.ACFunction.FUNCTION_SWITCH_WIND_SPEED.value, acStatus)) {
            _fanSpeed.value = speed
        }
    }

    fun toggleSwing() {
        val newSwing = !(_swing.value ?: true)
        val acStatus = freshStatus().apply {
            acWindDir = if (newSwing)
                Constants.ACSwing.SWING_ON.value
            else
                Constants.ACSwing.SWING_OFF.value
        }
        if (send(Constants.ACFunction.FUNCTION_SWITCH_SWING.value, acStatus)) {
            _swing.value = newSwing
        }
    }

    fun resetError() {
        _errorEvent.value = null
    }

    /**
     * 构造一个带有推荐默认值的 ACStatus，对齐 IRext 示例的
     * ControlHelper.translateKeyCode 中设定的基准值。
     */
    private fun freshStatus(): ACStatus {
        return ACStatus().apply {
            acPower = Constants.ACPower.POWER_OFF.value
            acMode = Constants.ACMode.MODE_COOL.value
            acTemp = Constants.ACTemperature.TEMP_24.value
            acWindSpeed = Constants.ACWindSpeed.SPEED_AUTO.value
            acWindDir = Constants.ACSwing.SWING_ON.value
            changeWindDir = 0
            acDisplay = 0
            acTimer = 0
            acSleep = 0
        }
    }

    private fun send(functionCode: Int, acStatus: ACStatus): Boolean {
        if (!initialized) {
            _errorEvent.value = "未初始化"
            return false
        }

        val pattern = irDecode.decodeBinary(functionCode, acStatus)
        Log.d(TAG, "send func=$functionCode, pattern len=${pattern.size}")

        if (pattern.isEmpty()) {
            _errorEvent.value = "红外编码为空"
            return false
        }

        val err = IrTransmitter.transmit(App.instance, pattern)
        if (err != null) {
            _errorEvent.value = "发射失败: $err"
            return false
        }
        return true
    }

    override fun onCleared() {
        super.onCleared()
        irDecode.closeBinary()
    }

    companion object {
        private const val TAG = "RemoteViewModel"
    }
}
