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

    // === 追踪空调真实当前状态 ===
    private var acPower = Constants.ACPower.POWER_OFF.value     // 1=OFF, 0=ON
    private var acMode = Constants.ACMode.MODE_COOL.value       // 0=COOL
    private var acTemp = Constants.ACTemperature.TEMP_24.value  // 8=24°C
    private var acWindSpeed = Constants.ACWindSpeed.SPEED_AUTO.value
    private var acWindDir = Constants.ACSwing.SWING_ON.value
    private var acDisplay = 0
    private var acSleep = 0
    private var acTimer = 0
    private var changeWindDir = 0

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
        val r = irDecode.openFile(categoryId, subCategory, codePath)
        if (r != 0) { _toast.value = "红外码库加载失败 (err=$r)"; return }

        // 查询此码库支持的功能范围
        try {
            val tr = irDecode.getTemperatureRange(Constants.ACMode.MODE_COOL.value)
            Log.d(TAG, "温度范围: ${tr.tempMin + 16} ~ ${tr.tempMax + 16}")
        } catch (_: Exception) {}

        initialized = true
        Log.d(TAG, "init success")
    }

    // === 操作 ===

    fun togglePower() {
        // 传入当前真实 power 状态，解码库据此判断切换方向
        val status = currentStatus()
        if (send(Constants.ACFunction.FUNCTION_SWITCH_POWER.value, status)) {
            // 解码库内部已切换，同步本地状态
            acPower = if (acPower == Constants.ACPower.POWER_OFF.value)
                Constants.ACPower.POWER_ON.value
            else
                Constants.ACPower.POWER_OFF.value
            _powerOn.value = acPower == Constants.ACPower.POWER_ON.value
        }
    }

    fun tempUp() {
        val status = currentStatus()
        Log.d(TAG, "tempUp: current temp=$acTemp (${acTemp + 16}°C)")
        if (send(Constants.ACFunction.FUNCTION_TEMPERATURE_UP.value, status)) {
            acTemp += 1
            _temperature.value = acTemp + 16
        }
    }

    fun tempDown() {
        val status = currentStatus()
        Log.d(TAG, "tempDown: current temp=$acTemp (${acTemp + 16}°C)")
        if (send(Constants.ACFunction.FUNCTION_TEMPERATURE_DOWN.value, status)) {
            acTemp -= 1
            _temperature.value = acTemp + 16
        }
    }

    fun cycleMode() {
        val status = currentStatus()
        if (send(Constants.ACFunction.FUNCTION_CHANGE_MODE.value, status)) {
            acMode = (acMode + 1) % 5
            _mode.value = acMode
        }
    }

    fun cycleWindSpeed() {
        val status = currentStatus()
        if (send(Constants.ACFunction.FUNCTION_SWITCH_WIND_SPEED.value, status)) {
            acWindSpeed = (acWindSpeed + 1) % 4
            _fanSpeed.value = acWindSpeed
        }
    }

    fun toggleSwing() {
        val status = currentStatus()
        if (send(Constants.ACFunction.FUNCTION_SWITCH_SWING.value, status)) {
            acWindDir = if (acWindDir == Constants.ACSwing.SWING_ON.value)
                Constants.ACSwing.SWING_OFF.value
            else
                Constants.ACSwing.SWING_ON.value
            _swing.value = acWindDir == Constants.ACSwing.SWING_ON.value
        }
    }

    fun resetToast() { _toast.value = null }

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

    // === 内部 ===

    /** 构造反映当前真实状态的 ACStatus */
    private fun currentStatus(): ACStatus {
        return ACStatus(acPower, acMode, acTemp, acWindSpeed, acWindDir,
            acDisplay, acSleep, acTimer, changeWindDir)
    }

    private fun send(functionCode: Int, acStatus: ACStatus): Boolean {
        if (!initialized) { _toast.value = "未初始化"; return false }

        val pattern = irDecode.decodeBinary(functionCode, acStatus)
        Log.d(TAG, "send func=$functionCode power=${acStatus.acPower} mode=${acStatus.acMode} temp=${acStatus.acTemp} -> patternLen=${pattern.size}")

        if (pattern.isEmpty()) {
            _toast.value = "红外编码为空 (func=$functionCode)"
            return false
        }
        val err = IrTransmitter.transmit(App.instance, pattern)
        if (err != null) { _toast.value = err; return false }
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
