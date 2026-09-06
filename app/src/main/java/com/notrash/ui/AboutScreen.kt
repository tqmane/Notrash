package com.notrash.ui

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notrash.R
import com.notrash.data.NotrashConfig
import com.notrash.ui.theme.NothingDotFontFamily
import com.notrash.ui.theme.NothingRed

@Composable
fun AboutScreen(onBack: () -> Unit, isModuleActive: Boolean) {
    val context = LocalContext.current
    val version = remember { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(top = 4.dp)) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
        }
        Column(
            Modifier.widthIn(max = 600.dp).fillMaxWidth().align(Alignment.CenterHorizontally)
        ) {
            Text(
                stringResource(R.string.about_header),
                fontFamily = NothingDotFontFamily,
                fontSize = 36.sp,
                lineHeight = 46.sp,
                modifier = Modifier.padding(start = 8.dp, top = 44.dp, bottom = 26.dp)
            )
            AboutMosaic(version, isModuleActive)
            Column(
                Modifier.padding(horizontal = 8.dp, vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    AboutFact(Icons.Outlined.Extension, stringResource(R.string.about_card_lsposed_title),
                        NotrashConfig.frameworkDescription ?: stringResource(R.string.status_inactive), Modifier.weight(1f))
                    AboutFact(Icons.Outlined.Apps, stringResource(R.string.about_features),
                        stringResource(R.string.about_features_value), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    AboutFact(Icons.Outlined.Memory, "Android",
                        Build.VERSION.RELEASE, Modifier.weight(1f))
                    AboutFact(Icons.Outlined.Smartphone, stringResource(R.string.device_model),
                        "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}", Modifier.weight(1f))
                }
                Text(context.packageName, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AboutMosaic(version: String, active: Boolean) {
    val card = MaterialTheme.colorScheme.surfaceContainerHigh
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gap = 4.dp
        val side = (maxWidth - gap) / 2
        val tileHeight = maxOf(side, 156.dp * fontScale.coerceAtMost(1.5f))
        val shape = RoundedCornerShape(22.dp)
        // The two short bridges match the connected cards on Nothing's device information page.
        Box(Modifier.offset(x = side - 4.dp, y = tileHeight * .34f)
            .size(gap + 8.dp, tileHeight * .32f).background(card))
        Box(Modifier.offset(x = side * .34f, y = tileHeight - 4.dp)
            .size(side * .32f, gap + 8.dp).background(card))
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(gap)) {
                Surface(Modifier.fillMaxWidth().height(tileHeight), shape = shape, color = card) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("VERSION\n$version", fontFamily = NothingDotFontFamily,
                            fontSize = 24.sp, lineHeight = 32.sp)
                        Box(Modifier.size(7.dp).clip(CircleShape).background(NothingRed))
                    }
                }
                Surface(Modifier.fillMaxWidth().height(tileHeight), shape = shape, color = card) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(if (active) R.string.module_connected else R.string.module_disconnected),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Notrash", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            Surface(Modifier.weight(1f).height(tileHeight * 2 + gap), shape = shape, color = card) {
                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(
                        Modifier.fillMaxWidth().fillMaxHeight(.84f).clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFF0C0D0D)).padding(vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("( N )", fontFamily = NothingDotFontFamily, fontSize = 14.sp, color = Color(0xFFB8BCBA))
                        Image(painterResource(R.drawable.ic_notrash_foreground), contentDescription = null,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f).scale(1.65f))
                        Text("NOTRASH", fontFamily = NothingDotFontFamily, fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutFact(icon: ImageVector, title: String, value: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(23.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
