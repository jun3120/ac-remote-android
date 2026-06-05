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

    // === UI 状态（独立于解码库内部状态） ===
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

        // 预热：首次 decodeBinary 可能只初始化内部状态，不产生有效编码
        val warmup = irDecode.decodeBinary(KEY_TEMP_UP, currentStatus())
        Log.d(TAG, "warmup patternLen=${warmup.size}")

        initialized = true
        Log.d(TAG, "init success")
    }

    // === 操作 ===
    // ACStatus 始终传固定基准值（对齐 IRext 示例 ControlHelper）。
    // 解码库内部自行跟踪状态变化，acStatus 仅用于校验合法性。
    // key_code 使用 native ir_ac_control 的实际映射值。

    fun togglePower() {
        if (send(KEY_POWER)) _powerOn.value = !(_powerOn.value ?: false)
    }

    fun tempUp() {
        if (send(KEY_TEMP_UP)) {
            acTemp += 1
            _temperature.value = acTemp + 16
        }
    }

    fun tempDown() {
        if (send(KEY_TEMP_DN)) {
            acTemp -= 1
            _temperature.value = acTemp + 16
        }
    }

    fun cycleMode() {
        if (send(KEY_MODE)) _mode.value = ((_mode.value ?: 0) + 1) % 5
    }

    fun cycleWindSpeed() {
        if (send(KEY_WIND_SPD)) _fanSpeed.value = ((_fanSpeed.value ?: 0) + 1) % 4
    }

    fun toggleSwing() {
        if (send(KEY_SWING)) _swing.value = !(_swing.value ?: false)
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

    // 温度需要跟踪（解码库从 acTemp 计算实际输出温度），其余用固定值
    private var acTemp = Constants.ACTemperature.TEMP_24.value

    /** 构造 ACStatus — 温度传递当前值，其余固定 */
    private fun currentStatus(): ACStatus {
        return ACStatus(
            Constants.ACPower.POWER_ON.value,
            Constants.ACMode.MODE_COOL.value,
            acTemp,
            Constants.ACWindSpeed.SPEED_AUTO.value,
            Constants.ACSwing.SWING_ON.value,
            0, 0, 0, 0
        )
    }

    private fun send(keyCode: Int): Boolean {
        if (!initialized) { _toast.value = "未初始化"; return false }

        val status = currentStatus()
        val pattern = irDecode.decodeBinary(keyCode, status)
        Log.d(TAG, "send key=$keyCode -> patternLen=${pattern.size}")

        if (pattern.isEmpty()) {
            _toast.value = "红外编码为空 (key=$keyCode)"
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

        // key_code 映射（根据实测修正）：
        //   0→POWER, 1→MODE?, 3→TEMP_UP(实测升温), 8→TEMP_DOWN,
        //   9→WIND_SPEED?, 10→SWING?
        private const val KEY_POWER    = 0
        private const val KEY_MODE     = 1
        private const val KEY_TEMP_UP  = 3   // 实测：空调升温
        private const val KEY_TEMP_DN  = 8   // 源码 alt TEMP_DOWN
        private const val KEY_WIND_SPD = 9
        private const val KEY_SWING    = 10
    }
}
