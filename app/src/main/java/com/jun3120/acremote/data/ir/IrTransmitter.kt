package com.jun3120.acremote.data.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log

object IrTransmitter {

    private const val CARRIER_FREQ = 38000

    fun hasIrEmitter(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        val has = manager?.hasIrEmitter() == true
        Log.d(TAG, "hasIrEmitter=$has")
        return has
    }

    /** @return null if success, error message if failed */
    fun transmit(context: Context, pattern: IntArray): String? {
        Log.d(TAG, "transmit called, pattern length=${pattern.size}")
        if (pattern.isEmpty()) return "pattern empty"

        val manager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        if (manager == null) {
            Log.e(TAG, "ConsumerIrManager is null — device may not support IR")
            return "ConsumerIrManager 为空"
        }

        if (!manager.hasIrEmitter()) {
            Log.e(TAG, "hasIrEmitter returned false")
            return "hasIrEmitter 返回 false"
        }

        try {
            manager.transmit(CARRIER_FREQ, pattern)
            Log.d(TAG, "transmit success")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "transmit exception", e)
            return "发射异常: ${e.message}"
        }
    }

    private const val TAG = "IrTransmitter"
}
