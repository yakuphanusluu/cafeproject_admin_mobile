package com.brewandbean.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.brewandbean.app.util.LanguageManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val clrPrimary = Color(0xFF1A1A2E)
private val clrAccent = Color(0xFFC8956C)
private val clrBg = Color(0xFFF3F0EB)
private val clrSurface = Color(0xFFFFFFFF)
private val clrTextMuted = Color(0xFF7A7A7A)

@Composable
fun HomeScreen(
    onNavigateToBarista: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    Box(modifier = Modifier.fillMaxSize().background(clrBg)) {
        TextButton(
            onClick = { com.brewandbean.app.util.LanguageManager.toggleLanguage() },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Text(if(isEn) "EN" else "TR", fontWeight = FontWeight.Bold, color = clrPrimary, fontSize = 16.sp)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Text("☕", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Brew & Bean",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = clrPrimary
            )
            Text(
                text = if(isEn) "Management System" else "Yonetim Sistemi",
                fontSize = 16.sp,
                color = clrTextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 48.dp)
            )

            // Barista Paneli Butonu
            Button(
                onClick = onNavigateToBarista,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                colors = ButtonDefaults.buttonColors(containerColor = clrPrimary),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(if(isEn) "☕   Barista Panel" else "☕   Barista Paneli", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Admin Paneli Butonu
            Button(
                onClick = onNavigateToAdmin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                colors = ButtonDefaults.buttonColors(containerColor = clrAccent),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(if(isEn) "👑  Admin Panel" else "👑  Admin Paneli", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if(isEn) "Use Barista Panel for live order tracking,\nand Admin Panel for reports and end-of-day." else "Canli siparis takibi icin Barista Paneli'ni,\nraporlar ve gunsonu icin Admin Paneli'ni kullanin.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = clrTextMuted,
                lineHeight = 20.sp
            )
        }
    }
}
