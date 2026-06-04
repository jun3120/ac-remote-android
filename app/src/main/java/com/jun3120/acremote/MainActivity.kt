package com.jun3120.acremote

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jun3120.acremote.data.ir.IrTransmitter
import com.jun3120.acremote.ui.brand.BrandPickerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnAddRemote: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAddRemote = findViewById(R.id.btn_add_remote)

        // 检查红外硬件
        if (!IrTransmitter.hasIrEmitter(this)) {
            btnAddRemote.text = "添加遥控器（设备不支持红外）"
            btnAddRemote.isEnabled = false
        } else {
            btnAddRemote.text = "添加遥控器"
        }

        btnAddRemote.setOnClickListener {
            startActivityForResult(
                Intent(this, BrandPickerActivity::class.java),
                REQUEST_BRAND_PICKER
            )
        }

        // 如果已有已保存的遥控器，显示列表
        // TODO: 读取本地保存的遥控器列表
    }

    @Deprecated("Use registerForActivityResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BRAND_PICKER && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "遥控器已添加，准备控制", Toast.LENGTH_SHORT).show()
            // TODO: Phase 3 - 跳转到遥控器控制界面
        }
    }

    companion object {
        private const val REQUEST_BRAND_PICKER = 100
    }
}
