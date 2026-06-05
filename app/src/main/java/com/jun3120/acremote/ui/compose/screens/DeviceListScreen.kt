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
