package com.jun3120.acremote.ui.brand

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        viewModel.remoteIndexes.observe(this) { showIndexes(it) }
        viewModel.loading.observe(this) { progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.downloadComplete.observe(this) { onDownloadComplete(it) }
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
            step = STEP_INDEX
            titleText.text = "选择遥控器型号"
            viewModel.selectedCategory?.let {
                viewModel.loadRemoteIndexes(it.id, brand.id)
            }
        }
        recyclerView.adapter = adapter
    }

    private fun showIndexes(indexes: List<RemoteIndex>) {
        if (step != STEP_INDEX) return
        val adapter = SimpleListAdapter(indexes.map {
            it to (it.remote ?: it.remoteMap ?: "型号 ${it.id}")
        }) { index ->
            viewModel.downloadBinFile(index as RemoteIndex)
        }
        recyclerView.adapter = adapter
    }

    private fun onDownloadComplete(index: RemoteIndex?) {
        if (index == null) return
        Toast.makeText(this, "遥控器码库已下载: ${index.remote}", Toast.LENGTH_SHORT).show()
        // 返回结果并关闭
        setResult(RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        when (step) {
            STEP_BRAND -> stepCategory()
            STEP_INDEX -> {
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
        private const val STEP_INDEX = 3
    }
}
