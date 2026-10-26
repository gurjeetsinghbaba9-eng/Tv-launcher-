package com.example.tvlauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

data class AppItem(val label: String, val pkg: String, val icon: ImageBitmap)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apps = loadApps()
        setContent { Home(apps) { launchApp(it) } }
    }

    private fun loadApps(): List<AppItem> {
        val pm = packageManager
        val tv = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val normal = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return (pm.queryIntentActivities(tv, 0) + pm.queryIntentActivities(normal, 0))
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .map {
                AppItem(
                    it.loadLabel(pm).toString(),
                    it.activityInfo.packageName,
                    it.loadIcon(pm).toBitmap(128, 128).asImageBitmap()
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(pkg: String) {
        val i = packageManager.getLeanbackLaunchIntentForPackage(pkg)
            ?: packageManager.getLaunchIntentForPackage(pkg)
        if (i != null) startActivity(i)
    }
}

@Composable
fun Home(apps: List<AppItem>, onClick: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Color(0xFF101418)).padding(48.dp)
    ) {
        Text("My Launcher", color = Color.White, fontSize = 32.sp)
        Spacer(Modifier.height(24.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(apps) { app -> AppCard(app, onClick) }
        }
    }
}

@Composable
fun AppCard(app: AppItem, onClick: (String) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .scale(if (focused) 1.08f else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E252D))
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick(app.pkg) }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Image(bitmap = app.icon, contentDescription = null, modifier = Modifier.size(72.dp)
        Image(bitmap = app.icon, contentDescription = null, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(8.dp))
        Text(app.label, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
