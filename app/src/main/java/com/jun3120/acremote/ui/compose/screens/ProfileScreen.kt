package com.jun3120.acremote.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jun3120.acremote.ui.compose.theme.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

private data class ProfileMenuItem(val icon: ImageVector, val label: String, val onClick: () -> Unit)

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val menuItems = listOf(
        ProfileMenuItem(Icons.AutoMirrored.Outlined.Chat, "意见反馈") { Toast.makeText(context, "即将上线", Toast.LENGTH_SHORT).show() },
        ProfileMenuItem(Icons.Outlined.Description, "用户协议") { Toast.makeText(context, "用户协议", Toast.LENGTH_SHORT).show() },
        ProfileMenuItem(Icons.Outlined.Shield, "隐私政策") { Toast.makeText(context, "隐私政策", Toast.LENGTH_SHORT).show() },
        ProfileMenuItem(Icons.Outlined.HeadsetMic, "联系客服") { Toast.makeText(context, "客服邮箱: support@example.com", Toast.LENGTH_SHORT).show() },
        ProfileMenuItem(Icons.Outlined.VerifiedUser, "隐私中心") { Toast.makeText(context, "隐私中心", Toast.LENGTH_SHORT).show() },
    )

    Column(modifier = modifier.fillMaxSize().background(Surface).padding(top = 40.dp, bottom = 96.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("我的", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        }
        Spacer(Modifier.height(24.dp))

        // 使用统计卡片
        val stats = com.jun3120.acremote.data.usage.UsageTracker.getStats(context)
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp)
                .shadow(2.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp))
                .background(SurfaceLowest).padding(20.dp)
        ) {
            Text("使用统计", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface, modifier = Modifier.padding(bottom = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("${stats.favoriteTemp}°C", "偏好温度")
                StatItem(if (stats.totalActions > 0) stats.favoriteMode else "--", "常用模式")
                StatItem(formatMinutes(stats.totalRuntimeMinutes), "累计运行")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Dark mode toggle
        var dark by remember { mutableStateOf(com.jun3120.acremote.ui.compose.theme.ThemeManager.isDark) }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text("深色模式", fontSize = 14.sp, color = OnSurfaceVariant, modifier = Modifier.weight(1f))
            androidx.compose.material3.Switch(checked = dark, onCheckedChange = { dark = it; com.jun3120.acremote.ui.compose.theme.ThemeManager.isDark = it })
        }

        Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp)) {
            Column(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(SurfaceLowest).padding(vertical = 8.dp)) {
                menuItems.forEachIndexed { index, item ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { item.onClick() }.padding(horizontal = 24.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, null, tint = Primary, modifier = Modifier.padding(end = 20.dp).size(20.dp))
                        Text(item.label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnSurface, modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.ChevronRight, null, tint = Outline, modifier = Modifier.size(16.dp))
                    }
                    if (index != menuItems.lastIndex) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 24.dp).background(SurfaceLow.copy(alpha = 0.8f)))
                    }
                }
            }
        }

        Text("版本号 v1.0.0", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Outline, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Primary)
        Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
    }
}

private fun formatMinutes(minutes: Long): String {
    if (minutes < 60) return "${minutes}m"
    val hours = minutes / 60
    val mins = minutes % 60
    return if (mins == 0L) "${hours}h" else "${hours}h${mins}m"
}
