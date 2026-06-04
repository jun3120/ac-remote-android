package com.jun3120.acremote

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jun3120.acremote.data.ir.IrTransmitter
import com.jun3120.acremote.ui.brand.BrandPickerActivity
import com.jun3120.acremote.ui.remote.RemoteControlActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnAddRemote: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAddRemote = findViewById(R.id.btn_add_remote)

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
    }

    @Deprecated("Use registerForActivityResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BRAND_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            val codePath = data.getStringExtra(BrandPickerActivity.EXTRA_CODE_PATH) ?: return
            val categoryId = data.getIntExtra(BrandPickerActivity.EXTRA_CATEGORY_ID, 1)
            val brandName = data.getStringExtra(BrandPickerActivity.EXTRA_BRAND_NAME) ?: "空调"

            val intent = Intent(this, RemoteControlActivity::class.java).apply {
                putExtra(RemoteControlActivity.EXTRA_CODE_PATH, codePath)
                putExtra(RemoteControlActivity.EXTRA_CATEGORY_ID, categoryId)
                putExtra(RemoteControlActivity.EXTRA_BRAND_NAME, brandName)
            }
            startActivity(intent)
        }
    }

    companion object {
        private const val REQUEST_BRAND_PICKER = 100
    }
}
