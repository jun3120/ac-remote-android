package com.jun3120.acremote.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jun3120.acremote.ui.compose.theme.OnSurface
import com.jun3120.acremote.ui.compose.theme.OnSurfaceVariant
import com.jun3120.acremote.ui.compose.theme.Outline
import com.jun3120.acremote.ui.compose.theme.Primary
import com.jun3120.acremote.ui.compose.theme.SurfaceHigh
import com.jun3120.acremote.ui.compose.theme.SurfaceLow
import com.jun3120.acremote.ui.compose.theme.SurfaceLowest

@Composable
fun SaveRemoteDialog(defaultName: String, onClose: () -> Unit, onSave: (String) -> Unit) {
    var name by remember(defaultName) { mutableStateOf(defaultName) }
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).shadow(24.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(SurfaceLowest).padding(24.dp)
            ) {
                Text("保存遥控器", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface, modifier = Modifier.padding(bottom = 24.dp))
                Text("设备名称", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OnSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                BasicTextField(
                    value = name, onValueChange = { name = it },
                    textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = OnSurface),
                    cursorBrush = SolidColor(Primary),
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceLow).padding(horizontal = 20.dp, vertical = 16.dp),
                    decorationBox = { inner ->
                        Box { if (name.isEmpty()) Text("输入设备名称", color = Outline, fontSize = 16.sp); inner() }
                    },
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(SurfaceLow).clickable(onClick = onClose).padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Text("取消", fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                    }
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Primary).clickable { onSave(name); onClose() }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Text("保存", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
