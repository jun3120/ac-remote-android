package com.jun3120.acremote.ui.compose.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jun3120.acremote.ui.compose.theme.*

private data class TipItem(val content: String)
private data class TipCategory(
    val title: String,
    val icon: ImageVector,
    val iconBg: Color,
    val tips: List<String>
)

private val tipCategories = listOf(
    TipCategory("温度节能", Icons.Outlined.AcUnit, Color(0xFFDBEAFE), listOf(
        "制冷设 26°C 最省电，每低 1°C 多耗电 7-10%",
        "制热设 20°C 最舒适，每高 1°C 多耗电 6-8%",
        "离开房间超过 1 小时建议关空调",
        "睡眠模式自动微调温度，省电又舒适",
    )),
    TipCategory("模式选择", Icons.Outlined.Air, Color(0xFFDBEAFE), listOf(
        "制冷模式：降温主力，适合 30°C 以上高温天",
        "除湿模式：梅雨季首选，体感凉爽不耗太多电",
        "送风模式：只吹风不制冷，春秋季配合电扇用",
        "自动模式：空调根据室温自动切换制冷/制热/送风",
    )),
    TipCategory("风向技巧", Icons.Outlined.Air, Color(0xFFE0F2FE), listOf(
        "冷空气下沉：制冷时风口朝上，冷气自然下沉扩散",
        "热空气上升：制热时风口朝下，暖气从脚底升起",
        "扫风开启可避免冷风直吹，房间温度更均匀",
    )),
    TipCategory("定时妙用", Icons.Outlined.Schedule, Color(0xFFEFF6FF), listOf(
        "睡前定 1-2 小时自动关，避免整夜吹空调着凉",
        "下班前 15 分钟用 APP 提前开机到家就凉快",
        "上班出门定时关，省去忘关空调的烦恼",
    )),
    TipCategory("清洁保养", Icons.Outlined.CleaningServices, Color(0xFFFEF3C7), listOf(
        "过滤网每月清洗 1 次，脏堵增加 20% 耗电",
        "散热片每季用专用清洗剂喷洗，可用空调清洗剂",
        "每年换季前请专业人员深度清洗 1 次",
    )),
    TipCategory("健康使用", Icons.Outlined.FavoriteBorder, Color(0xFFFFE4E6), listOf(
        "室内外温差不超过 8°C，进出不容易感冒",
        "每 2-3 小时开窗通风 10 分钟，保持空气清新",
        "空调房放盆水或加湿器，湿度保持 40-60%",
        "老人小孩房温度设 26-27°C，避免关节不适",
    )),
    TipCategory("省电妙招", Icons.Outlined.ElectricBolt, Color(0xFFFCE7F3), listOf(
        "配合吊扇/落地扇，体感降温 2-3°C，空调可调高",
        "白天拉上窗帘或百叶窗，减少阳光直射降温慢",
        "变频空调比定频空调省电 30-50%",
    )),
    TipCategory("故障排查", Icons.Outlined.Build, Color(0xFFF9FAFB), listOf(
        "不制冷先查过滤网是否脏堵、温度设定是否正确",
        "遥控器失灵换电池、清洁红外发射头尝试",
        "空调滴水可能是排水管堵塞或安装倾斜",
        "有异味说明过滤网或蒸发器发霉，需清洗",
    )),
    TipCategory("制冷剂知识", Icons.Outlined.Science, Color(0xFFF0FDF4), listOf(
        "R32 环保制冷剂为当前主流 GWP 仅为 R410A 的 1/3",
        "空调正常使用不会消耗制冷剂，不需定期加",
        "制冷效果明显下降才可能是制冷剂泄漏需专业检测",
    )),
    TipCategory("通用妙招", Icons.Outlined.Lightbulb, Color(0xFFFFF7ED), listOf(
        "选购空调看能效比 APF 值越高越省电",
        "安装位置尽量避开阳光直射和热源附近",
        "不频繁开关机，短时间离开用送风替代关机",
    )),
)

@Composable
fun TipsScreen(modifier: Modifier = Modifier) {
    var expandedIndex by remember { mutableStateOf(-1) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Surface).padding(top = 40.dp, bottom = 96.dp).padding(horizontal = 16.dp)
    ) {
        item {
            Text("空调技巧", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface, modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp))
        }
        itemsIndexed(tipCategories) { index, cat ->
            val expanded = expandedIndex == index
            val bgColor = if (expanded) cat.iconBg else SurfaceLowest
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))
                    .background(bgColor).animateContentSize().clickable { expandedIndex = if (expanded) -1 else index }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceLowest), contentAlignment = Alignment.Center) {
                        Icon(cat.icon, null, tint = Primary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(cat.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnSurface, modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.Air else Icons.Outlined.Build,
                        contentDescription = null,
                        tint = OnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (expanded) {
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Outline.copy(alpha = 0.3f)))
                    Spacer(Modifier.height(12.dp))
                    cat.tips.forEachIndexed { i, tip ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("${i + 1}.", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Primary, modifier = Modifier.width(24.dp))
                            Text(tip, fontSize = 14.sp, color = OnSurface, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
