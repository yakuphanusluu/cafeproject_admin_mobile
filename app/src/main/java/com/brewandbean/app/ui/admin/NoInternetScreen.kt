package com.brewandbean.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewandbean.app.util.LanguageManager

@Composable
fun NoInternetScreen() {
    val isEn by LanguageManager.isEnglish.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "📡",
                fontSize = 60.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if(isEn) "No Internet Connection" else "İnternet Bağlantısı Yok",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF1A1A2E),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if(isEn) "Please check your connection and try again." else "Lütfen internet bağlantınızı kontrol edin ve tekrar deneyin.",
                fontSize = 14.sp,
                color = Color(0xFF7A7A7A),
                textAlign = TextAlign.Center
            )
        }
    }
}
