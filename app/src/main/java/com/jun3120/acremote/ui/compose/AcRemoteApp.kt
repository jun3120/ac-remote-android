package com.jun3120.acremote.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.jun3120.acremote.data.local.RemotePreferences
import com.jun3120.acremote.ui.compose.components.BottomNavBar
import com.jun3120.acremote.ui.compose.components.BottomTab
import com.jun3120.acremote.ui.compose.screens.*
import com.jun3120.acremote.ui.compose.theme.Surface

enum class ViewState { Devices, SelectBrand, TestRemote, AcControl, Profile }

@Composable
fun AcRemoteApp() {
    val context = LocalContext.current
    var view by remember { mutableStateOf(ViewState.Devices) }
    var selectedBrand by remember { mutableStateOf("格力") }
    var selectedCategoryId by remember { mutableIntStateOf(1) }
    var selectedIndexesJson by remember { mutableStateOf("[]") }
    var showSaveModal by remember { mutableStateOf(false) }
    var currentCodePath by remember { mutableStateOf("") }
    var currentCategoryId by remember { mutableIntStateOf(1) }
    var currentSubCategory by remember { mutableIntStateOf(0) }
    var currentDeviceName by remember { mutableStateOf("未命名空调") }
    var pairingKey by remember { mutableIntStateOf(0) }
    var defaultSaveName by remember { mutableStateOf("") }

    // Load saved remotes
    var devices by remember { mutableStateOf(savedRemoteToDeviceUi(RemotePreferences.getSavedRemotes(context))) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (view) {
            ViewState.Devices -> DeviceListScreen(
                devices = devices,
                onAdd = { pairingKey++; view = ViewState.SelectBrand },
                onSelect = { name ->
                    val saved = RemotePreferences.getSavedRemotes(context)
                    val found = saved.find { it.brandName == name }
                    if (found != null) {
                        currentCodePath = found.codePath
                        currentCategoryId = found.categoryId
                        currentSubCategory = found.subCategory
                        currentDeviceName = found.displayName
                        view = ViewState.AcControl
                    }
                },
            )
            ViewState.SelectBrand -> SelectBrandScreen(
                onBack = { view = ViewState.Devices },
                onSelect = { brand, brandId ->
                    selectedBrand = brand
                    // Load indexes for this brand
                    selectedCategoryId = 1 // AC
                    Thread {
                        com.jun3120.acremote.App.instance.webAPIs.listRemoteIndexes(
                            1, brandId, "", "", 0,
                            object : net.irext.webapi.WebAPICallbacks.ListIndexesCallback {
                @Suppress("UNCHECKED_CAST")
                                override fun onListIndexesSuccess(list: List<net.irext.webapi.model.RemoteIndex>?) {
                                    selectedIndexesJson = Gson().toJson(list ?: emptyList<net.irext.webapi.model.RemoteIndex>())
                                    view = ViewState.TestRemote
                                }
                                override fun onListIndexesFailed() {}
                                override fun onListIndexesError() {}
                            })
                    }.start()
                },
            )
            ViewState.TestRemote -> {
                // key ensures fresh PairingViewModel each pairing session
                key(view.toString() + pairingKey.toString()) {
                    TestRemoteScreen(
                brand = selectedBrand,
                categoryId = selectedCategoryId,
                indexesJson = selectedIndexesJson,
                onBack = { view = ViewState.SelectBrand },
                onSuccess = { codePath, subCategory, brandName ->
                    currentCodePath = codePath
                    currentCategoryId = selectedCategoryId
                    currentSubCategory = subCategory
                    currentDeviceName = brandName
                    defaultSaveName = "${selectedBrand}空调"
                    view = ViewState.AcControl
                    showSaveModal = true
                },
            )
                }
            }
            ViewState.AcControl -> AcControlScreen(
                onBack = { view = ViewState.Devices },
                codePath = currentCodePath,
                categoryId = currentCategoryId,
                subCategory = currentSubCategory,
                deviceName = currentDeviceName,
                showSaveModal = showSaveModal,
                onCloseModal = { showSaveModal = false },
                onSaveDevice = { name ->
                    currentDeviceName = name
                    RemotePreferences.updateName(context, currentCodePath, name)
                    devices = savedRemoteToDeviceUi(RemotePreferences.getSavedRemotes(context))
                },
                defaultSaveName = defaultSaveName,
            )
            ViewState.Profile -> ProfileScreen(
                onBack = { view = ViewState.Devices },
            )
        }

        if (view == ViewState.Devices || view == ViewState.Profile) {
            BottomNavBar(
                active = if (view == ViewState.Profile) BottomTab.Profile else BottomTab.Remote,
                onChange = { tab -> view = if (tab == BottomTab.Profile) ViewState.Profile else ViewState.Devices },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
