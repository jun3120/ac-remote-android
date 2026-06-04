package com.jun3120.acremote.ui.remote

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jun3120.acremote.App
import com.jun3120.acremote.data.ir.IrController
import net.irext.decode.sdk.IRDecode
import net.irext.decode.sdk.bean.ACStatus
import net.irext.decode.sdk.utils.Constants

class RemoteViewModel : ViewModel() {

    private val irController = IrController(App.instance)
    private val irDecode = IRDecode.getInstance()

    // ACStatus — 空调当前状态，由 APP 层维护
    private val acStatus = ACStatus()

    // UI 状态
    private val _powerOn = MutableLiveData(false)
    val powerOn: LiveData<Boolean> = _powerOn

    private val _temperature = MutableLiveData(24)
    val temperature: LiveData<Int> = _temperature

    private val _mode = MutableLiveData(Constants.ACMode.MODE_AUTO.value)
    val mode: LiveData<Int> = _mode

    private val _fanSpeed = MutableLiveData(Constants.ACWindSpeed.SPEED_AUTO.value)
    val fanSpeed: LiveData<Int> = _fanSpeed

    private val _swing = MutableLiveData(true)
    val swing: LiveData<Boolean> = _swing

    // 温度范围
    private val _tempMin = MutableLiveData(16)
    val tempMin: LiveData<Int> = _tempMin
    private val _tempMax = MutableLiveData(30)
    val tempMax: LiveData<Int> = _tempMax

    // 支持的模式/风速
    private val _supportedModes = MutableLiveData(intArrayOf(1, 1, 1, 1, 1))
    val supportedModes: LiveData<IntArray> = _supportedModes

    private val _supportedWindSpeeds = MutableLiveData(intArrayOf(1, 1, 1, 1))
    val supportedWindSpeeds: LiveData<IntArray> = _supportedWindSpeeds

    // 品牌名
    private val _brandName = MutableLiveData("")
    val brandName: LiveData<String> = _brandName

    // 错误信息
    private val _errorEvent = MutableLiveData<String?>()
    val errorEvent: LiveData<String?> = _errorEvent

    private var categoryId = 0
    private var subCategory = 0
    private var initialized = false

    fun init(codePath: String, categoryId: Int, subCategory: Int, brandName: String) {
        if (initialized) return
        this.categoryId = categoryId
        this.subCategory = subCategory
        _brandName.value = brandName

        // 检查文件是否存在
        val file = java.io.File(codePath)
        Log.d(TAG, "Opening IR file: $codePath, exists=${file.exists()}, size=${file.length()}, category=$categoryId, subCategory=$subCategory")

        val result = irController.openFile(categoryId, subCategory, codePath)
        if (result != 0) {
            Log.e(TAG, "Failed to open IR code file: $codePath, error=$result, file exists=${file.exists()}")
            _errorEvent.value = "红外码库加载失败 (error=$result)"
            return
        }

        // 查询该码库支持的空调功能
        _supportedModes.value = irDecode.acSupportedMode
        val tempRange = irDecode.getTemperatureRange(Constants.ACMode.MODE_COOL.value)
        _tempMin.value = tempRange.tempMin + 16
        _tempMax.value = tempRange.tempMax + 16
        _supportedWindSpeeds.value = irDecode.getACSupportedWindSpeed(Constants.ACMode.MODE_COOL.value)

        initialized = true
        Log.d(TAG, "IR code loaded successfully: $codePath")
    }

    // ===== 用户操作 =====

    fun togglePower() {
        val newPower = !(_powerOn.value ?: false)
        acStatus.acPower = if (newPower)
            Constants.ACPower.POWER_ON.value
        else
            Constants.ACPower.POWER_OFF.value

        send(IrController.KEY_POWER)
        _powerOn.value = newPower
    }

    fun setMode(mode: Int) {
        acStatus.acMode = mode
        send(IrController.KEY_MODE)
        _mode.value = mode
    }

    fun tempUp() {
        val current = _temperature.value ?: 24
        val max = _tempMax.value ?: 30
        if (current >= max) return
        acStatus.acTemp = current - 16 + 1 // ACStatus temp: 0=16°C
        send(IrController.KEY_TEMP_UP)
        _temperature.value = current + 1
    }

    fun tempDown() {
        val current = _temperature.value ?: 24
        val min = _tempMin.value ?: 16
        if (current <= min) return
        acStatus.acTemp = current - 16 - 1
        send(IrController.KEY_TEMP_DOWN)
        _temperature.value = current - 1
    }

    fun setFanSpeed(speed: Int) {
        acStatus.acWindSpeed = speed
        send(IrController.KEY_WIND_SPEED)
        _fanSpeed.value = speed
    }

    fun toggleSwing() {
        val newSwing = !(_swing.value ?: true)
        acStatus.acWindDir = if (newSwing)
            Constants.ACSwing.SWING_ON.value
        else
            Constants.ACSwing.SWING_OFF.value
        send(IrController.KEY_SWING)
        _swing.value = newSwing
    }

    fun resetError() {
        _errorEvent.value = null
    }

    private fun send(keyCode: Int) {
        if (!initialized) {
            _errorEvent.value = "未初始化"
            return
        }
        val ok = irController.sendCommand(categoryId, subCategory, keyCode, acStatus)
        Log.d(TAG, "send keyCode=$keyCode, ok=$ok")
        if (!ok) {
            _errorEvent.value = "发射失败（检查红外硬件）"
        }
    }

    override fun onCleared() {
        super.onCleared()
        irController.close()
    }

    companion object {
        private const val TAG = "RemoteViewModel"
    }
}
