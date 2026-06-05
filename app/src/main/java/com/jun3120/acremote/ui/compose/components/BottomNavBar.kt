package com.jun3120.acremote.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jun3120.acremote.ui.compose.theme.OnSurfaceVariant
import com.jun3120.acremote.ui.compose.theme.Primary
import com.jun3120.acremote.ui.compose.theme.SurfaceLowest

enum class BottomTab { Remote, Profile }

@Composable
fun BottomNavBar(
    active: BottomTab,
    onChange: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, spotColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.04f))
            .background(SurfaceLowest)
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItem(Icons.Outlined.PhoneAndroid, "遥控", active == BottomTab.Remote) { onChange(BottomTab.Remote) }
        BottomNavItem(Icons.Outlined.Person, "我的", active == BottomTab.Profile) { onChange(BottomTab.Profile) }
    }
}

@Composable
private fun BottomNavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) Primary else OnSurfaceVariant
    val bg = if (selected) Primary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent
    Column(
        modifier = Modifier.width(80.dp).clip(RoundedCornerShape(12.dp)).background(bg).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.padding(bottom = 4.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = color)
    }
}
