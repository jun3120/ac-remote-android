package com.jun3120.acremote.data.ir

import android.content.Context
import android.util.Log
import net.irext.decode.sdk.IRDecode
import net.irext.decode.sdk.bean.ACStatus
import net.irext.decode.sdk.utils.Constants

/**
 * 红外遥控控制器 — 封装 IRext 解码 + 发射流程。
 * 参考 IRext 示例的 PhoneRemote.java 实现。
 */
class IrController(private val context: Context) {

    private val irDecode: IRDecode = IRDecode.getInstance()

    /** 从文件路径加载红外码库 */
    fun openFile(category: Int, subCategory: Int, filePath: String): Int {
        return irDecode.openFile(category, subCategory, filePath)
    }

    /** 从字节数组加载红外码库（用于缓存） */
    fun openBinary(category: Int, subCategory: Int, binaries: ByteArray): Int {
        return irDecode.openBinary(category, subCategory, binaries, binaries.size)
    }

    /** 解码并发射红外信号 */
    fun sendCommand(category: Int, subCategory: Int, keyCode: Int, acStatus: ACStatus): Boolean {
        val inputKeyCode = translateKeyCode(category, keyCode, acStatus)
        if (inputKeyCode < 0) return false

        val pattern = irDecode.decodeBinary(inputKeyCode, acStatus)
        Log.d(TAG, "decoded keyCode=$keyCode, inputKeyCode=$inputKeyCode, pattern length=${pattern.size}")

        if (pattern.isEmpty()) {
            Log.w(TAG, "decoded pattern is empty")
            return false
        }
        return IrTransmitter.transmit(context, pattern)
    }

    /** 获取支持的运行模式 */
    fun getSupportedModes(): IntArray {
        return irDecode.acSupportedMode
    }

    /** 获取指定模式下的温度范围 */
    fun getTemperatureRange(mode: Int) = irDecode.getTemperatureRange(mode)

    /** 获取指定模式支持的风速 */
    fun getSupportedWindSpeed(mode: Int): IntArray {
        return irDecode.getACSupportedWindSpeed(mode)
    }

    fun close() {
        irDecode.closeBinary()
    }

    companion object {
        private const val TAG = "IrController"

        // 遥控器按键码（与 IRext ACFunction 对应）
        const val KEY_POWER = 666
        const val KEY_TEMP_UP = 667
        const val KEY_TEMP_DOWN = 668
        const val KEY_MODE = 669
        const val KEY_WIND_SPEED = 670
        const val KEY_SWING = 671
        const val KEY_WIND_DIR = 672

        /**
         * 将 APP 按键码翻译为 IRext 解码库内部功能码。
         * ACStatus 在此过程中被设置为当前期望状态，
         * 解码库据此计算完整红外包。
         */
        fun translateKeyCode(category: Int, keyCode: Int, acStatus: ACStatus): Int {
            if (category != Constants.CategoryID.AIR_CONDITIONER.value) return keyCode

            return when (keyCode) {
                KEY_POWER -> Constants.ACFunction.FUNCTION_SWITCH_POWER.value
                KEY_TEMP_UP -> Constants.ACFunction.FUNCTION_TEMPERATURE_UP.value
                KEY_TEMP_DOWN -> Constants.ACFunction.FUNCTION_TEMPERATURE_DOWN.value
                KEY_MODE -> Constants.ACFunction.FUNCTION_CHANGE_MODE.value
                KEY_WIND_SPEED -> Constants.ACFunction.FUNCTION_SWITCH_WIND_SPEED.value
                KEY_SWING -> Constants.ACFunction.FUNCTION_SWITCH_SWING.value
                KEY_WIND_DIR -> Constants.ACFunction.FUNCTION_SWITCH_WIND_DIR.value
                else -> -1
            }
        }
    }
}
