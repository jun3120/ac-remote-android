package com.jun3120.acremote.ui.remote

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.jun3120.acremote.R
import net.irext.decode.sdk.utils.Constants

class RemoteControlActivity : AppCompatActivity() {

    private lateinit var vm: RemoteViewModel
    private lateinit var tvTemp: TextView
    private lateinit var tvMode: TextView
    private lateinit var tvFan: TextView
    private lateinit var tvBrand: TextView
    private lateinit var btnPower: Button
    private lateinit var btnSwing: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_control)

        vm = ViewModelProvider(this)[RemoteViewModel::class.java]

        tvBrand = findViewById(R.id.tv_brand)
        tvTemp = findViewById(R.id.tv_temperature)
        tvMode = findViewById(R.id.tv_mode)
        tvFan = findViewById(R.id.tv_fan)
        btnPower = findViewById(R.id.btn_power)
        btnSwing = findViewById(R.id.btn_swing)

        val codePath = intent.getStringExtra(EXTRA_CODE_PATH) ?: run {
            Toast.makeText(this, "缺少码库路径", Toast.LENGTH_SHORT).show()
            finish(); return
        }
        vm.init(
            codePath,
            intent.getIntExtra(EXTRA_CATEGORY_ID, Constants.CategoryID.AIR_CONDITIONER.value),
            intent.getIntExtra(EXTRA_SUB_CATEGORY, 0),
            intent.getStringExtra(EXTRA_BRAND_NAME) ?: "空调"
        )

        // 温度
        findViewById<Button>(R.id.btn_temp_up).setOnClickListener { vm.tempUp() }
        findViewById<Button>(R.id.btn_temp_down).setOnClickListener { vm.tempDown() }

        // 模式循环切
        findViewById<Button>(R.id.btn_mode).setOnClickListener { vm.cycleMode() }

        // 风速循环切
        findViewById<Button>(R.id.btn_fan_speed).setOnClickListener { vm.cycleWindSpeed() }

        // 开关
        btnPower.setOnClickListener { vm.togglePower() }

        // 扫风
        btnSwing.setOnClickListener { vm.toggleSwing() }

        // 观察数据
        vm.powerOn.observe(this) { on ->
            btnPower.text = if (on) "⏻  关  闭" else "⏻  开  机"
        }
        vm.temperature.observe(this) { tvTemp.text = "${it}°C" }
        vm.mode.observe(this) { tvMode.text = vm.modeDisplayName(it) }
        vm.fanSpeed.observe(this) { tvFan.text = vm.fanDisplayName(it) }
        vm.swing.observe(this) { s ->
            btnSwing.text = if (s) "扫风：开" else "扫风：关"
        }
        vm.brandName.observe(this) { tvBrand.text = it }
        vm.toast.observe(this) {
            if (!it.isNullOrEmpty()) { Toast.makeText(this, it, Toast.LENGTH_SHORT).show(); vm.resetToast() }
        }
    }

    companion object {
        const val EXTRA_CODE_PATH = "code_path"
        const val EXTRA_CATEGORY_ID = "category_id"
        const val EXTRA_SUB_CATEGORY = "sub_category"
        const val EXTRA_BRAND_NAME = "brand_name"
    }
}
