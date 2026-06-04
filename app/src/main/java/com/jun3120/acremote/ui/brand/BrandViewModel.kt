package com.jun3120.acremote.ui.brand

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jun3120.acremote.App
import com.jun3120.acremote.data.local.RemotePreferences
import com.jun3120.acremote.data.local.SavedRemote
import net.irext.decode.sdk.utils.Constants
import net.irext.webapi.WebAPICallbacks
import net.irext.webapi.model.Brand
import net.irext.webapi.model.Category
import net.irext.webapi.model.RemoteIndex
import java.io.File
import java.io.InputStream

class BrandViewModel : ViewModel() {

    private val webAPIs = App.instance.webAPIs

    // 品类列表
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // 品牌列表
    private val _brands = MutableLiveData<List<Brand>>()
    val brands: LiveData<List<Brand>> = _brands

    // 遥控器型号列表
    private val _remoteIndexes = MutableLiveData<List<RemoteIndex>>()
    val remoteIndexes: LiveData<List<RemoteIndex>> = _remoteIndexes

    // 加载状态
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    // 下载完成事件
    private val _downloadComplete = MutableLiveData<RemoteIndex?>()
    val downloadComplete: LiveData<RemoteIndex?> = _downloadComplete

    // 当前选中的品类
    var selectedCategory: Category? = null
    // 当前选中的品牌
    var selectedBrand: Brand? = null
    // 下载的 bin 文件路径
    var binFilePath: String? = null
    // 选中型号的 subCategory（irOpen 需要）
    var selectedSubCategory: Int = 0

    fun loadCategories() {
        _loading.value = true
        Thread {
            webAPIs.listCategories(0, 50, object : WebAPICallbacks.ListCategoriesCallback {
                override fun onListCategoriesSuccess(categories: List<Category>?) {
                    _categories.postValue(categories ?: emptyList())
                    _loading.postValue(false)
                }

                override fun onListCategoriesFailed() {
                    _loading.postValue(false)
                    Log.w(TAG, "list categories failed")
                }

                override fun onListCategoriesError() {
                    _loading.postValue(false)
                    Log.e(TAG, "list categories error")
                }
            })
        }.start()
    }

    fun loadBrands(categoryId: Int) {
        _loading.value = true
        Thread {
            webAPIs.listBrands(categoryId, 0, 50, object : WebAPICallbacks.ListBrandsCallback {
                override fun onListBrandsSuccess(brands: List<Brand>?) {
                    _brands.postValue(brands ?: emptyList())
                    _loading.postValue(false)
                }

                override fun onListBrandsFailed() {
                    _loading.postValue(false)
                    Log.w(TAG, "list brands failed")
                }

                override fun onListBrandsError() {
                    _loading.postValue(false)
                    Log.e(TAG, "list brands error")
                }
            })
        }.start()
    }

    fun loadRemoteIndexes(categoryId: Int, brandId: Int) {
        _loading.value = true
        Thread {
            webAPIs.listRemoteIndexes(
                categoryId, brandId, "", "", 0,
                object : WebAPICallbacks.ListIndexesCallback {
                    override fun onListIndexesSuccess(indexes: List<RemoteIndex>?) {
                        _remoteIndexes.postValue(indexes ?: emptyList())
                        _loading.postValue(false)
                    }

                    override fun onListIndexesFailed() {
                        _loading.postValue(false)
                        Log.w(TAG, "list indexes failed")
                    }

                    override fun onListIndexesError() {
                        _loading.postValue(false)
                        Log.e(TAG, "list indexes error")
                    }
                })
        }.start()
    }

    fun downloadBinFile(remoteIndex: RemoteIndex) {
        selectedSubCategory = remoteIndex.subCate
        _loading.value = true
        Thread {
            webAPIs.downloadBin(
                remoteIndex.remoteMap,
                remoteIndex.id,
                object : WebAPICallbacks.DownloadBinCallback {
                    override fun onDownloadBinSuccess(inputStream: InputStream?) {
                        inputStream?.let { saveBinFile(remoteIndex, it) }
                        _loading.postValue(false)
                        _downloadComplete.postValue(remoteIndex)
                    }

                    override fun onDownloadBinFailed() {
                        _loading.postValue(false)
                        Log.w(TAG, "download bin failed")
                    }

                    override fun onDownloadBinError() {
                        _loading.postValue(false)
                        Log.e(TAG, "download bin error")
                    }
                })
        }.start()
    }

    private fun saveBinFile(remoteIndex: RemoteIndex, inputStream: InputStream) {
        val dir = File(App.instance.filesDir, "bin")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "${remoteIndex.remoteMap}.bin")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        binFilePath = file.absolutePath
        Log.d(TAG, "bin file saved: ${file.absolutePath}, size=${file.length()}")

        // 保存遥控器记录到本地
        val brandName = selectedBrand?.name ?: "未知"
        val subCate = remoteIndex.subCate
        RemotePreferences.addRemote(
            App.instance,
            SavedRemote(
                codePath = file.absolutePath,
                categoryId = selectedCategory?.id ?: 1,
                subCategory = subCate,
                brandName = brandName
            )
        )
    }

    /** 获取品类名称 */
    fun getCategoryName(category: Category): String {
        return when (category.id) {
            Constants.CategoryID.AIR_CONDITIONER.value -> "空调"
            Constants.CategoryID.TV.value -> "电视"
            Constants.CategoryID.STB.value -> "机顶盒"
            Constants.CategoryID.FAN.value -> "风扇"
            Constants.CategoryID.PROJECTOR.value -> "投影仪"
            Constants.CategoryID.STEREO.value -> "音响"
            Constants.CategoryID.LIGHT.value -> "灯"
            Constants.CategoryID.CLEANING_ROBOT.value -> "扫地机器人"
            Constants.CategoryID.AIR_CLEANER.value -> "空气净化器"
            Constants.CategoryID.HEATER.value -> "热水器"
            else -> category.name ?: "其他"
        }
    }

    companion object {
        private const val TAG = "BrandViewModel"
    }
}
