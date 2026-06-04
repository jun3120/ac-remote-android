package com.jun3120.acremote.data.ir

import android.content.Context
import android.hardware.ConsumerIrManager

/**
 * ConsumerIrManager 封装 — 检查红外硬件并发射红外时序数据。
 */
object IrTransmitter {

    private const val CARRIER_FREQ = 38000

    fun hasIrEmitter(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        return manager?.hasIrEmitter() == true
    }

    fun transmit(context: Context, pattern: IntArray): Boolean {
        if (pattern.isEmpty()) return false

        val manager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
            ?: return false

        if (!manager.hasIrEmitter()) return false

        manager.transmit(CARRIER_FREQ, pattern)
        return true
    }
}
