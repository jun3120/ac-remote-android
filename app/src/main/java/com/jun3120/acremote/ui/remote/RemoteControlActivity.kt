package com.jun3120.acremote.ui.remote

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.jun3120.acremote.App
import com.jun3120.acremote.R
import net.irext.decode.sdk.utils.Constants

class RemoteControlActivity : AppCompatActivity() {

    private lateinit var viewModel: RemoteViewModel

    private lateinit var tvBrand: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvMode: TextView
    private lateinit var btnPower: Button
    private lateinit var btnTempUp: Button
    private lateinit var btnTempDown: Button
    private lateinit var btnSwing: Button

    // Mode buttons
    private lateinit var btnCool: Button
    private lateinit var btnHeat: Button
    private lateinit var btnAuto: Button
    private lateinit var btnFan: Button
    private lateinit var btnDry: Button

    // Fan speed buttons
    private lateinit var btnSpeedAuto: Button
    private lateinit var btnSpeedLow: Button
    private lateinit var btnSpeedMid: Button
    private lateinit var btnSpeedHigh: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_control)

        viewModel = ViewModelProvider(this)[RemoteViewModel::class.java]

        initViews()
        setupObservers()

        // 从 BrandPicker 传入的码库路径初始化
        val codePath = intent.getStringExtra(EXTRA_CODE_PATH)
        val categoryId = intent.getIntExtra(EXTRA_CATEGORY_ID, Constants.CategoryID.AIR_CONDITIONER.value)
        val brandName = intent.getStringExtra(EXTRA_BRAND_NAME) ?: "空调"

        if (codePath.isNullOrEmpty()) {
            Toast.makeText(this, "未指定码库文件", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.init(codePath, categoryId, brandName)
        tvBrand.text = brandName
    }

    private fun initViews() {
        tvBrand = findViewById(R.id.tv_brand)
        tvTemp = findViewById(R.id.tv_temperature)
        tvMode = findViewById(R.id.tv_mode)
        btnPower = findViewById(R.id.btn_power)
        btnTempUp = findViewById(R.id.btn_temp_up)
        btnTempDown = findViewById(R.id.btn_temp_down)
        btnSwing = findViewById(R.id.btn_swing)

        btnCool = findViewById(R.id.btn_cool)
        btnHeat = findViewById(R.id.btn_heat)
        btnAuto = findViewById(R.id.btn_auto)
        btnFan = findViewById(R.id.btn_fan)
        btnDry = findViewById(R.id.btn_dry)

        btnSpeedAuto = findViewById(R.id.btn_speed_auto)
        btnSpeedLow = findViewById(R.id.btn_speed_low)
        btnSpeedMid = findViewById(R.id.btn_speed_mid)
        btnSpeedHigh = findViewById(R.id.btn_speed_high)

        btnPower.setOnClickListener { viewModel.togglePower() }
        btnTempUp.setOnClickListener { viewModel.tempUp() }
        btnTempDown.setOnClickListener { viewModel.tempDown() }
        btnSwing.setOnClickListener { viewModel.toggleSwing() }

        btnCool.setOnClickListener { viewModel.setMode(Constants.ACMode.MODE_COOL.value) }
        btnHeat.setOnClickListener { viewModel.setMode(Constants.ACMode.MODE_HEAT.value) }
        btnAuto.setOnClickListener { viewModel.setMode(Constants.ACMode.MODE_AUTO.value) }
        btnFan.setOnClickListener { viewModel.setMode(Constants.ACMode.MODE_FAN.value) }
        btnDry.setOnClickListener { viewModel.setMode(Constants.ACMode.MODE_DEHUMIDITY.value) }

        btnSpeedAuto.setOnClickListener { viewModel.setFanSpeed(Constants.ACWindSpeed.SPEED_AUTO.value) }
        btnSpeedLow.setOnClickListener { viewModel.setFanSpeed(Constants.ACWindSpeed.SPEED_LOW.value) }
        btnSpeedMid.setOnClickListener { viewModel.setFanSpeed(Constants.ACWindSpeed.SPEED_MEDIUM.value) }
        btnSpeedHigh.setOnClickListener { viewModel.setFanSpeed(Constants.ACWindSpeed.SPEED_HIGH.value) }
    }

    private fun setupObservers() {
        viewModel.powerOn.observe(this) { on ->
            btnPower.text = if (on) "关闭" else "开机"
        }

        viewModel.temperature.observe(this) { temp ->
            tvTemp.text = "${temp}°C"
        }

        viewModel.mode.observe(this) { mode ->
            tvMode.text = when (mode) {
                Constants.ACMode.MODE_COOL.value -> "制冷"
                Constants.ACMode.MODE_HEAT.value -> "制热"
                Constants.ACMode.MODE_AUTO.value -> "自动"
                Constants.ACMode.MODE_FAN.value -> "送风"
                Constants.ACMode.MODE_DEHUMIDITY.value -> "除湿"
                else -> "未知"
            }
        }

        viewModel.swing.observe(this) { swing ->
            btnSwing.text = if (swing) "扫风中" else "扫风关"
        }

        viewModel.errorEvent.observe(this) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                viewModel.resetError()
            }
        }
    }

    companion object {
        const val EXTRA_CODE_PATH = "code_path"
        const val EXTRA_CATEGORY_ID = "category_id"
        const val EXTRA_BRAND_NAME = "brand_name"
    }
}
