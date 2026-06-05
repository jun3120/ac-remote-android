package com.jun3120.acremote.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.jun3120.acremote.data.usage.UsageStats
import com.jun3120.acremote.data.usage.UsageTracker
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jun3120.acremote.data.local.SavedRemote
import com.jun3120.acremote.ui.compose.theme.*

data class DeviceUiIcon(val icon: ImageVector, val iconBackground: Color, val iconTint: Color)
data class DeviceUi(val name: String, val status: String, val icon: ImageVector, val iconBackground: Color, val iconTint: Color, val offline: Boolean = false)

private val iconOptions = listOf(
    DeviceUiIcon(Icons.Outlined.AcUnit, Blue100, Primary),
    DeviceUiIcon(Icons.Outlined.Air, Cyan100, Cyan700),
    DeviceUiIcon(Icons.Outlined.Hotel, Gray100, Gray500),
    DeviceUiIcon(Icons.Outlined.Business, Orange100, Orange600),
)

fun savedRemoteToDeviceUi(remotes: List<SavedRemote>): List<DeviceUi> {
    return remotes.mapIndexed { idx, r ->
        val iconChoice = iconOptions[idx % iconOptions.size]
        DeviceUi(name = r.displayName, status = "已连接", icon = iconChoice.icon, iconBackground = iconChoice.iconBackground, iconTint = iconChoice.iconTint)
    }
}

@Composable
fun DeviceListScreen(
    devices: List<DeviceUi>,
    onAdd: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(Surface)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 40.dp, bottom = 96.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.PhoneAndroid, null, tint = Primary, modifier = Modifier.size(24.dp))
                    Text("空调遥控器", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
                }
                Spacer(Modifier.size(48.dp))
            }
            Spacer(Modifier.height(32.dp))
            Text("我的设备", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Text("共 ${devices.size} 个连接的遥控器", fontSize = 14.sp, color = OnSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

            if (devices.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = true),
                ) {
                    items(devices) { device ->
                        DeviceCard(device, onClick = { onSelect(device.name) })
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
                    Text("暂无设备，点击添加开始配对", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurfaceVariant)
                }
            }

            // 节能建议卡片
            val ctx = LocalContext.current
            val stats = remember { UsageTracker.getStats(ctx) }
            if (stats.totalActions > 0) {
                val tip = getEnergyTip(stats)
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth().background(PrimaryContainer, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 18.sp); Spacer(Modifier.width(8.dp))
                        Text("节能建议", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnPrimaryContainer)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(tip, fontSize = 13.sp, color = OnPrimaryContainer.copy(alpha = 0.8f), lineHeight = 20.sp)
                }
            }
        }
        FloatingActionButton(
            onClick = onAdd, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 96.dp).size(56.dp).shadow(8.dp, CircleShape, spotColor = Primary.copy(alpha = 0.3f)),
            containerColor = Primary, contentColor = Color.White, shape = CircleShape,
        ) { Icon(Icons.Filled.Add, "添加设备", modifier = Modifier.size(32.dp)) }
    }
}

@Composable
private fun DeviceCard(device: DeviceUi, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLowest)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(device.iconBackground), contentAlignment = Alignment.Center) {
            Icon(device.icon, null, tint = device.iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(device.name, fontWeight = FontWeight.SemiBold, color = OnSurface, maxLines = 1, fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (device.offline) Outline else Primary))
            Text(device.status, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}

private fun getEnergyTip(stats: UsageStats): String {
    return when {
        stats.favoriteTemp > 24 -> "夏季制冷建议设在 26°C，每调高 1°C 可节电约 7%。您当前偏好 ${stats.favoriteTemp}°C。"
        stats.favoriteTemp < 22 -> "冬季制热建议设在 20°C，每调低 1°C 可节电约 10%。"
        stats.totalRuntimeMinutes > 480 -> "您今日已运行超过 8 小时，建议定时关闭或开窗通风片刻。"
        stats.modes.getOrDefault("制冷", 0) > stats.modes.getOrDefault("制热", 0) -> "制冷模式下搭配风扇使用可提升体感降温效果，节省空调能耗。"
        else -> "出门前记得关空调，使用定时功能可避免忘关造成的能源浪费。"
    }
}
