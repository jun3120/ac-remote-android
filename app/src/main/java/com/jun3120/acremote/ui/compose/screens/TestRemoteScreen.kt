package com.jun3120.acremote.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jun3120.acremote.ui.brand.PairingViewModel
import com.jun3120.acremote.ui.compose.theme.*

@Composable
fun TestRemoteScreen(
    brand: String,
    categoryId: Int,
    indexesJson: String,
    onBack: () -> Unit,
    onSuccess: (codePath: String, subCategory: Int, brandName: String) -> Unit,
    pairingKey: Int = 0,
    modifier: Modifier = Modifier,
) {
    val vm: PairingViewModel = viewModel(key = "pairing_$pairingKey")

    LaunchedEffect(Unit) {
        val type = object : com.google.gson.reflect.TypeToken<List<net.irext.webapi.model.RemoteIndex>>() {}.type
        val indexes: List<net.irext.webapi.model.RemoteIndex> = try { com.google.gson.Gson().fromJson(indexesJson, type) ?: emptyList() } catch (e: Exception) { emptyList() }
        vm.init(categoryId, brand, indexes)
    }

    val state by vm.state.observeAsState(PairingViewModel.State.INIT)
    val currentName by vm.currentName.observeAsState("")
    val progressText by vm.progressText.observeAsState("")

    val pairResult by vm.pairResult.observeAsState()

    LaunchedEffect(state, pairResult) {
        val r = pairResult
        if (state == PairingViewModel.State.SUCCESS && r != null) {
            onSuccess(r.codePath, r.subCategory, r.brandName)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Surface, TestRemoteGradientEnd))).padding(top = 40.dp, bottom = 48.dp)) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(40.dp), contentAlignment = Alignment.Center) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = OnSurface)
            }
            Text(brand, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
        }
        Spacer(Modifier.height(64.dp))

        Column(modifier = Modifier.fillMaxWidth().weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (currentName.isEmpty()) "方案 1 / ?"  else currentName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Outline, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 6.dp))
                Text("正在匹配您的空调", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            }
            Spacer(Modifier.height(64.dp))

            Box(modifier = Modifier.size(224.dp).shadow(24.dp, CircleShape, spotColor = Primary.copy(alpha = 0.3f)).clip(CircleShape).background(Primary).clickable { vm.testPower() }, contentAlignment = Alignment.Center) {
                if (state == PairingViewModel.State.DOWNLOADING || state == PairingViewModel.State.RETRYING)
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(48.dp))
                else
                    Icon(Icons.Filled.PowerSettingsNew, "电源", tint = Color.White, modifier = Modifier.size(80.dp))
            }

            Spacer(Modifier.height(64.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 32.dp)) {
                Text("点击电源按钮测试开关", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(bottom = 8.dp))
                Text(if (progressText.isEmpty()) "请确认您的设备是否有反应" else progressText, fontSize = 14.sp, color = OnSurfaceVariant)
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(SurfaceLow).clickable { vm.skipCurrent() }.padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    Text("否", fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                }
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Primary).clickable { vm.confirmMatch() }.padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    Text("是", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Text("手动输入型号", fontSize = 14.sp, color = Outline, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth().padding(top = 24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
