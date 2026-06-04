package com.jun3120.acremote.ui.brand

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.jun3120.acremote.R
import net.irext.webapi.model.Brand
import net.irext.webapi.model.Category
import net.irext.webapi.model.RemoteIndex

class BrandPickerActivity : AppCompatActivity() {

    private lateinit var viewModel: BrandViewModel
    private lateinit var titleText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private var step = STEP_CATEGORY

    private val pairingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            // 配对成功，将结果传回上一层
            setResult(RESULT_OK, result.data)
            finish()
        } else {
            // 配对取消或失败，回到品牌选择
            step = STEP_BRAND
            titleText.text = "选择品牌"
            viewModel.selectedCategory?.let { viewModel.loadBrands(it.id) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brand_picker)

        titleText = findViewById(R.id.tv_title)
        recyclerView = findViewById(R.id.rv_list)
        progressBar = findViewById(R.id.progress_bar)

        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProvider(this)[BrandViewModel::class.java]

        setupObservers()
        stepCategory()
    }

    private fun setupObservers() {
        viewModel.categories.observe(this) { showCategories(it) }
        viewModel.brands.observe(this) { showBrands(it) }
        viewModel.remoteIndexes.observe(this) { onIndexesLoaded(it) }
        viewModel.loading.observe(this) { progressBar.visibility = if (it) View.VISIBLE else View.GONE }
    }

    private fun stepCategory() {
        step = STEP_CATEGORY
        titleText.text = "选择电器类型"
        viewModel.loadCategories()
    }

    private fun showCategories(categories: List<Category>) {
        if (step != STEP_CATEGORY) return
        val adapter = SimpleListAdapter(categories.map { it to viewModel.getCategoryName(it) }) { category ->
            viewModel.selectedCategory = category as Category
            step = STEP_BRAND
            titleText.text = "选择品牌"
            viewModel.loadBrands(category.id)
        }
        recyclerView.adapter = adapter
    }

    private fun showBrands(brands: List<Brand>) {
        if (step != STEP_BRAND) return
        val adapter = SimpleListAdapter(brands.map { it to (it.name ?: "未知") }) { brand ->
            viewModel.selectedBrand = brand as Brand
            // 选择品牌后，加载该品牌的型号列表，进入配对流程
            step = STEP_PAIRING
            titleText.text = "准备配对..."
            viewModel.selectedCategory?.let {
                viewModel.loadRemoteIndexes(it.id, brand.id)
            }
        }
        recyclerView.adapter = adapter
    }

    private fun onIndexesLoaded(indexes: List<RemoteIndex>) {
        if (step != STEP_PAIRING) return
        if (indexes.isEmpty()) {
            titleText.text = "该品牌暂无可用型号，请返回"
            return
        }

        // 品牌下有型号，直接进入配对流程
        val categoryId = viewModel.selectedCategory?.id ?: 1
        val brandName = viewModel.selectedBrand?.name ?: "未知"
        val indexesJson = Gson().toJson(indexes)

        val intent = Intent(this, PairingActivity::class.java).apply {
            putExtra(PairingActivity.EXTRA_CATEGORY_ID, categoryId)
            putExtra(PairingActivity.EXTRA_BRAND_NAME, brandName)
            putExtra(PairingActivity.EXTRA_INDEXES, indexesJson)
        }
        pairingLauncher.launch(intent)
    }

    override fun onBackPressed() {
        when (step) {
            STEP_BRAND -> stepCategory()
            STEP_PAIRING -> {
                step = STEP_BRAND
                titleText.text = "选择品牌"
                viewModel.selectedCategory?.let { viewModel.loadBrands(it.id) }
            }
            else -> super.onBackPressed()
        }
    }

    companion object {
        private const val STEP_CATEGORY = 1
        private const val STEP_BRAND = 2
        private const val STEP_PAIRING = 3

        const val EXTRA_CODE_PATH = "code_path"
        const val EXTRA_CATEGORY_ID = "category_id"
        const val EXTRA_SUB_CATEGORY = "sub_category"
        const val EXTRA_BRAND_NAME = "brand_name"
    }
}
