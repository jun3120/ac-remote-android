package com.jun3120.acremote.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jun3120.acremote.App
import com.jun3120.acremote.ui.compose.theme.*

private data class BrandItem(val name: String, val en: String)

@Composable
fun SelectBrandScreen(
    onBack: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var brands by remember { mutableStateOf<List<BrandItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Load brands from IRext API
        Thread {
            App.instance.webAPIs.listBrands(1, 0, 50, object : net.irext.webapi.WebAPICallbacks.ListBrandsCallback {
                override fun onListBrandsSuccess(list: List<net.irext.webapi.model.Brand>?) {
                    brands = list?.map { BrandItem(it.name ?: "未知", "") } ?: emptyList()
                    loading = false
                }
                override fun onListBrandsFailed() { loading = false }
                override fun onListBrandsError() { loading = false }
            })
        }.start()
    }

    Column(modifier = modifier.fillMaxSize().background(Surface).verticalScroll(rememberScrollState()).padding(top = 40.dp, bottom = 80.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Primary) }
            Text("选择空调品牌", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            IconButton(onClick = {}) { Icon(Icons.Outlined.MoreVert, "更多", tint = OnSurfaceVariant) }
        }
        Spacer(Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(80.dp).shadow(8.dp, CircleShape, spotColor = Primary.copy(alpha = 0.2f)).clip(CircleShape).background(Primary), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.PhoneAndroid, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Text("请选择您的空调品牌以匹配最合适的遥控方案。", fontSize = 14.sp, color = OnSurfaceVariant, modifier = Modifier.padding(top = 16.dp))
        }
        Spacer(Modifier.height(32.dp))

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceLow).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Search, null, tint = Outline, modifier = Modifier.size(20.dp))
                BasicTextField(value = "", onValueChange = {}, textStyle = TextStyle(fontSize = 14.sp, color = OnSurface), cursorBrush = SolidColor(Primary), modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                    decorationBox = { inner -> Box { Text("搜索品牌...", color = Outline, fontSize = 14.sp); inner() } })
            }
        }
        Spacer(Modifier.height(24.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("热门品牌", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface, modifier = Modifier.padding(bottom = 16.dp))

                val rows = brands.chunked(2)
                rows.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { brand ->
                            HotBrandCard(brand, { onSelect(brand.name) }, Modifier.weight(1f))
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun HotBrandCard(brand: BrandItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.shadow(2.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(SurfaceLowest).clickable(onClick = onClick).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(brand.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface, modifier = Modifier.padding(bottom = 2.dp))
        if (brand.en.isNotEmpty()) Text(brand.en, fontSize = 11.sp, color = Outline, letterSpacing = 2.sp)
    }
}
