package com.jun3120.acremote.ui.compose.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jun3120.acremote.ui.compose.components.SaveRemoteDialog
import com.jun3120.acremote.ui.compose.theme.*
import com.jun3120.acremote.ui.remote.RemoteViewModel

private enum class AcMode(val label: String, val icon: ImageVector) {
    Cool("制冷", Icons.Filled.AcUnit), Heat("制热", Icons.Filled.Whatshot), Auto("自动", Icons.Outlined.AutoMode), Fan("送风", Icons.Filled.Air), Dry("除湿", Icons.Outlined.WaterDrop);
    fun next() = entries[(ordinal + 1) % entries.size]
}
private enum class FanSpeed(val label: String) {
    Low("低"), Medium("中"), High("高"), Auto("自动");
    val statusLabel get() = if (this == Auto) "自动风速" else "${label}风速"
    fun next() = entries[(ordinal + 1) % entries.size]
}
private enum class SwingMode(val label: String) {
    On("开启"), Off("关闭");
    fun next() = entries[(ordinal + 1) % entries.size]
}

@Composable
fun AcControlScreen(
    onBack: () -> Unit,
    codePath: String,
    categoryId: Int,
    subCategory: Int,
    deviceName: String,
    showSaveModal: Boolean,
    onCloseModal: () -> Unit,
    onSaveDevice: (String) -> Unit,
    defaultSaveName: String = deviceName,
    modifier: Modifier = Modifier,
) {
    val vm: RemoteViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(codePath) { vm.init(codePath, categoryId, subCategory, deviceName) }

    val powerOn by vm.powerOn.observeAsState(true)
    val temp by vm.temperature.observeAsState(26)
    val mode by vm.mode.observeAsState(0)
    val fanSpeed by vm.fanSpeed.observeAsState(0)
    val swing by vm.swing.observeAsState(false)
    val toast by vm.toast.observeAsState()

    LaunchedEffect(toast) {
        if (!toast.isNullOrEmpty()) { Toast.makeText(context, toast, Toast.LENGTH_SHORT).show(); vm.resetToast() }
    }

    Box(modifier = modifier.fillMaxSize().background(Surface)) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 40.dp, bottom = 32.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = OnSurface) }
                Text(deviceName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                IconButton(onClick = {}) { Icon(Icons.Outlined.MoreVert, "更多", tint = OnSurfaceVariant) }
            }

            Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(24.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.height(224.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(temp.toString(), fontSize = 112.sp, fontWeight = FontWeight.Bold,
                            color = if (powerOn) Primary else Outline.copy(alpha = 0.7f), letterSpacing = (-2).sp, lineHeight = 112.sp)
                        Text("°C", fontSize = 40.sp, fontWeight = FontWeight.SemiBold,
                            color = if (powerOn) Primary else Outline.copy(alpha = 0.7f), modifier = Modifier.padding(top = 16.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 12.dp)) {
                        val modeLabels = listOf("制冷","制热","自动","送风","除湿")
                        Text(modeLabels.getOrElse(mode) { "制冷" }, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (powerOn) Primary else Outline)
                        Box(Modifier.size(4.dp).clip(CircleShape).background(Outline.copy(alpha = 0.5f)))
                        val fanLabels = listOf("自动风速","低风速","中风速","高风速")
                        Text(fanLabels.getOrElse(fanSpeed) { "自动风速" }, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurfaceVariant)
                    }

                    Row(modifier = Modifier.padding(top = 20.dp).clip(RoundedCornerShape(50)).background(SurfaceLowest)
                        .border(1.dp, if (powerOn) Primary.copy(alpha = 0.1f) else SurfaceHigh, RoundedCornerShape(50))
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(if (powerOn) Primary else Outline))
                        Text(if (powerOn) "正在运行" else "已关机", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(48.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    CircleControlButton(onClick = { vm.tempDown() }, enabled = powerOn) {
                        Icon(Icons.Filled.Remove, "降温", tint = OnSurface.copy(alpha = if (powerOn) 1f else 0.3f), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.size(24.dp))
                    Box(modifier = Modifier.size(110.dp).shadow(12.dp, CircleShape, spotColor = if (powerOn) Primary.copy(alpha = 0.3f) else Outline.copy(alpha = 0.2f))
                        .clip(CircleShape).background(if (powerOn) Primary else Outline).clickable { vm.togglePower() },
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PowerSettingsNew, "电源", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                    Spacer(Modifier.size(24.dp))
                    CircleControlButton(onClick = { vm.tempUp() }, enabled = powerOn) {
                        Icon(Icons.Filled.Add, "升温", tint = OnSurface.copy(alpha = if (powerOn) 1f else 0.3f), modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(Modifier.height(48.dp))

                Column(modifier = Modifier.fillMaxWidth().alpha(if (powerOn) 1f else 0.5f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val modeIcons = listOf(Icons.Filled.AcUnit, Icons.Filled.Whatshot, Icons.Outlined.AutoMode, Icons.Filled.Air, Icons.Outlined.WaterDrop)
                        val modeLabels = listOf("制冷","制热","自动","送风","除湿")
                        SecondaryControlCard("模式", modeIcons.getOrElse(mode) { Icons.Filled.AcUnit }, modeLabels.getOrElse(mode) { "制冷" }, powerOn, { vm.cycleMode() }, Modifier.weight(1f))
                        val fanLabels = listOf("自动","低","中","高")
                        SecondaryControlCard("风速", Icons.Filled.Air, fanLabels.getOrElse(fanSpeed) { "自动" }, powerOn, { vm.cycleWindSpeed() }, Modifier.weight(1f))
                    }
                    SecondaryControlCard("扫风", Icons.Filled.Sync, if (swing) "开启" else "关闭", powerOn, { vm.toggleSwing() }, Modifier.fillMaxWidth())
                }
            }
        }

        if (showSaveModal) SaveRemoteDialog(defaultName = defaultSaveName, onClose = onCloseModal, onSave = { name -> onSaveDevice(name) })
    }
}

@Composable
private fun CircleControlButton(onClick: () -> Unit, enabled: Boolean, content: @Composable () -> Unit) {
    Box(modifier = Modifier.size(64.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(SurfaceLowest).clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun SecondaryControlCard(label: String, icon: ImageVector, value: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.shadow(2.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(SurfaceLowest).clickable(enabled = enabled, onClick = onClick).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Outline, modifier = Modifier.padding(bottom = 12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(24.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
        }
    }
}
