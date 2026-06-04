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

    // === UI 状态 ===
    private val _powerOn = MutableLiveData(false)
    val powerOn: LiveData<Boolean> = _powerOn

    private val _temperature = MutableLiveData(24)
    val temperature: LiveData<Int> = _temperature

    private val _mode = MutableLiveData(Constants.ACMode.MODE_COOL.value)
    val mode: LiveData<Int> = _mode

    private val _fanSpeed = MutableLiveData(Constants.ACWindSpeed.SPEED_AUTO.value)
    val fanSpeed: LiveData<Int> = _fanSpeed

    private val _swing = MutableLiveData(false)
    val swing: LiveData<Boolean> = _swing

    private val _brandName = MutableLiveData("")
    val brandName: LiveData<String> = _brandName

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?> = _toast

    private var initialized = false

    fun init(codePath: String, categoryId: Int, subCategory: Int, brandName: String) {
        if (initialized) return
        _brandName.value = brandName

        val file = java.io.File(codePath)
        Log.d(TAG, "openFile: $codePath exists=${file.exists()} size=${file.length()} cat=$categoryId sub=$subCategory")

        irDecode.closeBinary()
        val result = irDecode.openFile(categoryId, subCategory, codePath)
        if (result != 0) {
            Log.e(TAG, "openFile failed: $result")
            _toast.value = "红外码库加载失败 (err=$result)"
            return
        }
        initialized = true
        Log.d(TAG, "init success")
    }

    // === 操作：IRext 约定 — ACStatus 始终用固定基准值，解码库自行跟踪状态 ===

    fun togglePower() {
        // 永远传 POWER_OFF，解码库内部切换
        if (send(Constants.ACFunction.FUNCTION_SWITCH_POWER.value)) {
            _powerOn.value = !(_powerOn.value ?: false)
        }
    }

    fun tempUp() {
        if (send(Constants.ACFunction.FUNCTION_TEMPERATURE_UP.value)) {
            val cur = _temperature.value ?: 24
            _temperature.value = cur + 1
        }
    }

    fun tempDown() {
        if (send(Constants.ACFunction.FUNCTION_TEMPERATURE_DOWN.value)) {
            val cur = _temperature.value ?: 24
            _temperature.value = cur - 1
        }
    }

    fun cycleMode() {
        if (send(Constants.ACFunction.FUNCTION_CHANGE_MODE.value)) {
            val cur = _mode.value ?: Constants.ACMode.MODE_COOL.value
            _mode.value = (cur + 1) % 5
        }
    }

    fun cycleWindSpeed() {
        if (send(Constants.ACFunction.FUNCTION_SWITCH_WIND_SPEED.value)) {
            val cur = _fanSpeed.value ?: Constants.ACWindSpeed.SPEED_AUTO.value
            _fanSpeed.value = (cur + 1) % 4
        }
    }

    fun modeDisplayName(m: Int): String = when (m) {
        Constants.ACMode.MODE_COOL.value -> "制冷"
        Constants.ACMode.MODE_HEAT.value -> "制热"
        Constants.ACMode.MODE_AUTO.value -> "自动"
        Constants.ACMode.MODE_FAN.value -> "送风"
        Constants.ACMode.MODE_DEHUMIDITY.value -> "除湿"
        else -> "未知"
    }

    fun fanDisplayName(s: Int): String = when (s) {
        Constants.ACWindSpeed.SPEED_AUTO.value -> "自动风速"
        Constants.ACWindSpeed.SPEED_LOW.value -> "低风速"
        Constants.ACWindSpeed.SPEED_MEDIUM.value -> "中风速"
        Constants.ACWindSpeed.SPEED_HIGH.value -> "高风速"
        else -> "未知"
    }

    fun toggleSwing() {
        if (send(Constants.ACFunction.FUNCTION_SWITCH_SWING.value)) {
            _swing.value = !(_swing.value ?: false)
        }
    }

    fun resetToast() {
        _toast.value = null
    }

    // === 内部 ===

    /**
     * 发送红外指令。
     * 严格遵循 IRext 示例：构造固定基准 ACStatus，仅通过 functionCode 表达操作。
     */
    private fun send(functionCode: Int): Boolean {
        if (!initialized) {
            _toast.value = "未初始化"
            return false
        }

        // 固定基准 ACStatus，每次全新构造，永远不改值
        val acStatus = ACStatus().apply {
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

        val pattern = irDecode.decodeBinary(functionCode, acStatus)
        Log.d(TAG, "decode func=$functionCode patternLen=${pattern.size}")

        if (pattern.isEmpty()) {
            _toast.value = "红外编码为空 (func=$functionCode)"
            return false
        }

        val err = IrTransmitter.transmit(App.instance, pattern)
        if (err != null) {
            _toast.value = err
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
