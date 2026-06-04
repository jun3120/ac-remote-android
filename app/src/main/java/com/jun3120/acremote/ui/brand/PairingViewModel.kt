package com.jun3120.acremote.ui.brand

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jun3120.acremote.App
import com.jun3120.acremote.data.ir.IrTransmitter
import com.jun3120.acremote.data.local.RemotePreferences
import com.jun3120.acremote.data.local.SavedRemote
import net.irext.decode.sdk.IRDecode
import net.irext.decode.sdk.bean.ACStatus
import net.irext.decode.sdk.utils.Constants
import net.irext.webapi.WebAPICallbacks
import net.irext.webapi.model.RemoteIndex
import java.io.File
import java.io.InputStream

/**
 * 配对流程 ViewModel：逐一下载/测试型号码库，直到匹配成功。
 */
class PairingViewModel : ViewModel() {

    private val webAPIs = App.instance.webAPIs
    private val irDecode = IRDecode.getInstance()

    // 待测试的型号列表
    private var indexes: List<RemoteIndex> = emptyList()
    private var currentPosition = 0

    // 当前配对信息
    var categoryId: Int = Constants.CategoryID.AIR_CONDITIONER.value
        private set
    var brandName: String = ""
        private set

    // 当前型号名称
    private val _currentName = MutableLiveData("")
    val currentName: LiveData<String> = _currentName

    // 进度文字
    private val _progressText = MutableLiveData("")
    val progressText: LiveData<String> = _progressText

    // 配对状态: downloading, ready, testing, success, failed, skip
    private val _state = MutableLiveData(State.INIT)
    val state: LiveData<State> = _state

    // 配对结果（成功时带回 SavedRemote）
    private val _pairResult = MutableLiveData<SavedRemote?>()
    val pairResult: LiveData<SavedRemote?> = _pairResult

    // 当前 .bin 路径
    private var currentBinPath: String? = null
    private var currentSubCategory: Int = 0

    // 错误信息
    private val _errorEvent = MutableLiveData<String?>()
    val errorEvent: LiveData<String?> = _errorEvent

    fun init(categoryId: Int, brandName: String, indexes: List<RemoteIndex>) {
        if (_state.value != State.INIT) return
        this.categoryId = categoryId
        this.brandName = brandName
        this.indexes = indexes

        if (indexes.isEmpty()) {
            _state.value = State.FAILED
            _progressText.value = "该品牌暂无可用型号"
            return
        }

        // 从第一个型号开始配对
        currentPosition = 0
        startCurrent()
    }

    private fun startCurrent() {
        if (currentPosition >= indexes.size) {
            _state.value = State.FAILED
            _progressText.value = "已尝试全部 ${indexes.size} 个型号，未找到匹配"
            return
        }

        val index = indexes[currentPosition]
        _currentName.value = index.remote ?: index.remoteMap ?: "型号 ${index.id}"
        _progressText.value = "正在准备第 ${currentPosition + 1}/${indexes.size} 个型号..."
        _state.value = State.DOWNLOADING

        currentSubCategory = index.subCate
        downloadAndLoad(index)
    }

    private fun downloadAndLoad(index: RemoteIndex) {
        Thread {
            webAPIs.downloadBin(
                index.remoteMap,
                index.id,
                object : WebAPICallbacks.DownloadBinCallback {
                    override fun onDownloadBinSuccess(inputStream: InputStream?) {
                        inputStream?.let { stream ->
                            val file = saveBinFile(index, stream)
                            currentBinPath = file.absolutePath
                            Log.d(TAG, "bin downloaded: ${file.absolutePath}, size=${file.length()}")

                            val result = irDecode.openFile(categoryId, index.subCate, file.absolutePath)
                            Log.d(TAG, "irOpen result=$result")
                            if (result == 0) {
                                _progressText.postValue("第 ${currentPosition + 1}/${indexes.size} 个型号，点击测试按钮")
                                _state.postValue(State.READY)
                            } else {
                                // 加载失败，自动跳到下一个
                                Log.w(TAG, "irOpen failed, skip to next")
                                _state.postValue(State.RETRYING)
                                moveToNext()
                            }
                        } ?: run {
                            _errorEvent.postValue("下载失败")
                            _state.postValue(State.RETRYING)
                            moveToNext()
                        }
                    }

                    override fun onDownloadBinFailed() {
                        _errorEvent.postValue("下载失败")
                        _state.postValue(State.RETRYING)
                        moveToNext()
                    }

                    override fun onDownloadBinError() {
                        _errorEvent.postValue("下载失败")
                        _state.postValue(State.RETRYING)
                        moveToNext()
                    }
                })
        }.start()
    }

    private fun saveBinFile(index: RemoteIndex, inputStream: InputStream): File {
        val dir = File(App.instance.filesDir, "bin")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "${index.remoteMap}.bin")
        file.outputStream().use { output -> inputStream.copyTo(output) }
        return file
    }

    /** 用户点击测试按钮 — 发送开关信号 */
    fun testPower() {
        if (_state.value != State.READY) return

        _state.value = State.TESTING
        _progressText.value = "正在发射红外信号..."

        // 构造 ACStatus 并发送开关信号
        val acStatus = ACStatus().apply {
            acPower = Constants.ACPower.POWER_ON.value
            acMode = Constants.ACMode.MODE_COOL.value
            acTemp = Constants.ACTemperature.TEMP_24.value
            acWindSpeed = Constants.ACWindSpeed.SPEED_AUTO.value
            acWindDir = Constants.ACSwing.SWING_ON.value
        }

        val pattern = irDecode.decodeBinary(
            Constants.ACFunction.FUNCTION_SWITCH_POWER.value, acStatus
        )
        Log.d(TAG, "test power: pattern length=${pattern.size}")

        val err = if (pattern.isNotEmpty()) {
            IrTransmitter.transmit(App.instance, pattern)
        } else "pattern empty"

        _progressText.postValue(
            if (err == null) "信号已发射！空调有响应吗？" else "发射失败: $err"
        )
        _state.postValue(if (err == null) State.AWAIT_CONFIRM else State.READY)
    }

    /** 用户确认匹配成功 */
    fun confirmMatch() {
        val binPath = currentBinPath ?: run {
            _errorEvent.value = "码库路径为空"
            return
        }

        // 保存已配对的遥控器
        RemotePreferences.addRemote(
            App.instance,
            SavedRemote(
                codePath = binPath,
                categoryId = categoryId,
                subCategory = currentSubCategory,
                brandName = brandName
            )
        )

        _progressText.value = "配对成功！"
        _state.value = State.SUCCESS
        _pairResult.value = SavedRemote(binPath, categoryId, currentSubCategory, brandName)
    }

    /** 用户跳过当前型号，试下一个 */
    fun skipCurrent() {
        _progressText.value = "跳过，正在准备下一个..."
        _state.value = State.RETRYING
        moveToNext()
    }

    private fun moveToNext() {
        // 关闭当前打开的码库
        irDecode.closeBinary()
        currentPosition++
        startCurrent()
    }

    fun resetError() {
        _errorEvent.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // 注意：配对成功后不关闭码库，由下一个 Activity 接管
        // 只在配对失败/取消时关闭
        if (_state.value != State.SUCCESS) {
            irDecode.closeBinary()
        }
    }

    enum class State {
        INIT, DOWNLOADING, READY, TESTING, AWAIT_CONFIRM, SUCCESS, FAILED, RETRYING
    }

    companion object {
        private const val TAG = "PairingViewModel"
    }
}
