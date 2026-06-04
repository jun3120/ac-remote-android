package com.jun3120.acremote.ui.brand

import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jun3120.acremote.R
import com.jun3120.acremote.data.local.SavedRemote
import net.irext.webapi.model.RemoteIndex

class PairingActivity : AppCompatActivity() {

    private lateinit var viewModel: PairingViewModel

    private lateinit var tvTitle: TextView
    private lateinit var tvModelName: TextView
    private lateinit var tvProgress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnTest: Button
    private lateinit var btnMatch: Button
    private lateinit var btnSkip: Button
    private lateinit var layoutTest: View
    private lateinit var layoutConfirm: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing)

        viewModel = ViewModelProvider(this)[PairingViewModel::class.java]

        initViews()
        setupObservers()

        val categoryId = intent.getIntExtra(EXTRA_CATEGORY_ID, 1)
        val brandName = intent.getStringExtra(EXTRA_BRAND_NAME) ?: "未知"
        val indexesJson = intent.getStringExtra(EXTRA_INDEXES) ?: "[]"
        val type = object : TypeToken<List<RemoteIndex>>() {}.type
        val indexes: List<RemoteIndex> = try {
            Gson().fromJson(indexesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        tvTitle.text = "$brandName — 配对中"
        viewModel.init(categoryId, brandName, indexes)
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tv_pairing_title)
        tvModelName = findViewById(R.id.tv_model_name)
        tvProgress = findViewById(R.id.tv_progress)
        progressBar = findViewById(R.id.progress_bar)
        btnTest = findViewById(R.id.btn_test)
        btnMatch = findViewById(R.id.btn_match)
        btnSkip = findViewById(R.id.btn_skip)
        layoutTest = findViewById(R.id.layout_test)
        layoutConfirm = findViewById(R.id.layout_confirm)
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                PairingViewModel.State.INIT,
                PairingViewModel.State.DOWNLOADING -> {
                    progressBar.visibility = View.VISIBLE
                    layoutTest.visibility = View.GONE
                    layoutConfirm.visibility = View.GONE
                }
                PairingViewModel.State.READY -> {
                    progressBar.visibility = View.GONE
                    layoutTest.visibility = View.VISIBLE
                    layoutConfirm.visibility = View.GONE
                }
                PairingViewModel.State.TESTING -> {
                    progressBar.visibility = View.GONE
                    layoutTest.visibility = View.GONE
                    layoutConfirm.visibility = View.GONE
                    vibrate()
                }
                PairingViewModel.State.AWAIT_CONFIRM -> {
                    progressBar.visibility = View.GONE
                    layoutTest.visibility = View.GONE
                    layoutConfirm.visibility = View.VISIBLE
                    vibrate()
                }
                PairingViewModel.State.RETRYING -> {
                    progressBar.visibility = View.VISIBLE
                    layoutTest.visibility = View.GONE
                    layoutConfirm.visibility = View.GONE
                }
                PairingViewModel.State.SUCCESS -> {
                    progressBar.visibility = View.GONE
                    layoutTest.visibility = View.GONE
                    layoutConfirm.visibility = View.GONE
                    Toast.makeText(this, "配对成功！", Toast.LENGTH_SHORT).show()
                }
                PairingViewModel.State.FAILED -> {
                    progressBar.visibility = View.GONE
                    layoutTest.visibility = View.GONE
                    layoutConfirm.visibility = View.GONE
                    tvProgress.text = "未找到匹配型号，请检查：\n1. 品牌是否正确\n2. 手机红外是否对准空调"
                }
            }
        }

        viewModel.currentName.observe(this) {
            tvModelName.text = it
        }

        viewModel.progressText.observe(this) {
            tvProgress.text = it
        }

        viewModel.pairResult.observe(this) { saved ->
            if (saved != null) {
                val data = Intent().apply {
                    putExtra(EXTRA_CODE_PATH, saved.codePath)
                    putExtra(EXTRA_CATEGORY_ID, saved.categoryId)
                    putExtra(EXTRA_SUB_CATEGORY, saved.subCategory)
                    putExtra(EXTRA_BRAND_NAME, saved.brandName)
                }
                setResult(RESULT_OK, data)
                finish()
            }
        }

        viewModel.errorEvent.observe(this) { err ->
            if (!err.isNullOrEmpty()) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
                viewModel.resetError()
            }
        }

        // 按钮事件
        btnTest.setOnClickListener { viewModel.testPower() }
        btnMatch.setOnClickListener { viewModel.confirmMatch() }
        btnSkip.setOnClickListener { viewModel.skipCurrent() }
    }

    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(100,
            android.os.VibrationEffect.DEFAULT_AMPLITUDE))
    }

    companion object {
        const val EXTRA_CATEGORY_ID = "category_id"
        const val EXTRA_BRAND_NAME = "brand_name"
        const val EXTRA_INDEXES = "indexes_json"
        const val EXTRA_CODE_PATH = "code_path"
        const val EXTRA_SUB_CATEGORY = "sub_category"
    }
}
