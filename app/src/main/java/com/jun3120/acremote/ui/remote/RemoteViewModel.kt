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

    // 解码库只追踪模式/温度/风速/扫风（电源始终传 ON，实际开关由解码库 apply_function 处理）
    private var acMode = Constants.ACMode.MODE_COOL.value
    private var acTemp = Constants.ACTemperature.TEMP_24.value
    private var acWindSpeed = Constants.ACWindSpeed.SPEED_AUTO.value
    private var acWindDir = Constants.ACSwing.SWING_ON.value

    // === UI 状态 ===
    private val _powerOn = MutableLiveData(true)
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
        Log.d(TAG, "openFile: $codePath exists=${file.exists()} size=${file.length()}")

        val r = irDecode.openFile(categoryId, subCategory, codePath)
        if (r != 0) { _toast.value = "红外码库加载失败 (err=$r)"; return }

        try {
            val tr = irDecode.getTemperatureRange(Constants.ACMode.MODE_COOL.value)
            Log.d(TAG, "temp range: ${tr.tempMin + 16} ~ ${tr.tempMax + 16}")
        } catch (_: Exception) {}

        initialized = true
        Log.d(TAG, "init success")
    }

    // === 操作 ===
    // 使用 IRext Remote.java 定义的原始 keyCode（0-11），而非 ACFunction 枚举值（1-7）。
    // Native ir_ac_control 内部做 key_code→function_code 映射。
    // 始终传 acPower=POWER_ON，保证全部 apply 函数运行。

    fun togglePower() {
        if (send(KEY_POWER)) {
            _powerOn.value = !(_powerOn.value ?: false)
        }
    }

    fun tempUp() {
        if (send(KEY_PLUS)) {
            acTemp += 1
            _temperature.value = acTemp + 16
        }
    }

    fun tempDown() {
        if (send(KEY_MINUS)) {
            acTemp -= 1
            _temperature.value = acTemp + 16
        }
    }

    fun cycleMode() {
        if (send(KEY_RIGHT)) {
            acMode = (acMode + 1) % 5
            _mode.value = acMode
        }
    }

    fun cycleWindSpeed() {
        if (send(KEY_UP)) {
            acWindSpeed = (acWindSpeed + 1) % 4
            _fanSpeed.value = acWindSpeed
        }
    }

    fun toggleSwing() {
        if (send(KEY_OK)) {
            acWindDir = if (acWindDir == Constants.ACSwing.SWING_ON.value)
                Constants.ACSwing.SWING_OFF.value else Constants.ACSwing.SWING_ON.value
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

    /** 构造 ACStatus — acPower 始终为 ON，确保解码库运行全部 apply 函数 */
    private fun currentStatus(): ACStatus {
        return ACStatus(
            Constants.ACPower.POWER_ON.value,  // always ON
            acMode,
            acTemp,
            acWindSpeed,
            acWindDir,
            0,  // display
            0,  // sleep
            0,  // timer
            0   // changeWindDir
        )
    }

    private fun send(functionCode: Int): Boolean {
        if (!initialized) { _toast.value = "未初始化"; return false }

        val status = currentStatus()
        val pattern = irDecode.decodeBinary(functionCode, status)
        Log.d(TAG, "send func=$functionCode mode=${status.acMode} temp=${status.acTemp} -> patternLen=${pattern.size}")

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

        // IRext Remote.java 原始 keyCode 常量
        private const val KEY_POWER = 0
        private const val KEY_UP = 1
        private const val KEY_DOWN = 2
        private const val KEY_LEFT = 3
        private const val KEY_RIGHT = 4
        private const val KEY_OK = 5
        private const val KEY_PLUS = 6
        private const val KEY_MINUS = 7
        private const val KEY_BACK = 8
        private const val KEY_HOME = 9
        private const val KEY_MENU = 10
    }
}
