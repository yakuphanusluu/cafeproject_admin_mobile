package com.brewandbean.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.brewandbean.app.util.LanguageManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewandbean.app.data.model.AdminOrder

// Sitedeki renkler
private val clrPrimary = Color(0xFF1A1A2E)
private val clrAccent = Color(0xFFC8956C)
private val clrAccentLight = Color(0xFFF5EBE0)
private val clrBg = Color(0xFFF3F0EB)
private val clrSurface = Color(0xFFFFFFFF)
private val clrTextMuted = Color(0xFF7A7A7A)

private val statusAlindi = Color(0xFF3B82F6)
private val statusHazirlaniyor = Color(0xFFF59E0B)
private val statusHazir = Color(0xFF10B981)
private val statusTeslim = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BaristaScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEn by LanguageManager.isEnglish.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Siparisleri gruplara ayir (Eski siparisler ustte, yeniler altta olacak sekilde sirala)
    val alindiOrders = orders.filter { it.status == "alindi" }.sortedBy { it.id }
    val hazirlaniyorOrders = orders.filter { it.status == "hazirlaniyor" }.sortedBy { it.id }
    val hazirOrders = orders.filter { it.status == "hazir" }.sortedBy { it.id }
    val teslimOrders = orders.filter { it.status == "teslim_edildi" }.sortedByDescending { it.id } // Teslim edilenlerde en son teslim edilen ustte kalsin

    Scaffold(
        containerColor = clrBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("☕ Barista", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp, maxLines = 1)
                        val activeOrdersCount = alindiOrders.size + hazirlaniyorOrders.size + hazirOrders.size
                        Box(modifier = Modifier.background(clrAccent, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text("$activeOrdersCount", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { com.brewandbean.app.util.LanguageManager.toggleLanguage() }) {
                        Text(if(isEn) "EN" else "TR", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), RoundedCornerShape(50)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if(isEn) "Live" else "Canli", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { viewModel.refreshOrders() }) {
                        Icon(Icons.Default.Refresh, contentDescription = if(isEn) "Refresh" else "Yenile", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = clrPrimary)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading && orders.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = clrAccent)
            } else if (alindiOrders.isEmpty() && hazirlaniyorOrders.isEmpty() && hazirOrders.isEmpty()) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if(isEn) "No active orders..." else "Aktif siparis bulunmuyor...", color = clrTextMuted, fontSize = 16.sp)
                }
            } else {
                // Tek bir liste, gruplar halinde — kolay scroll
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // HAZIR grubu (En ustte, acil teslim edilmesi gerekenler)
                    if (hazirOrders.isNotEmpty()) {
                        item(key = "header-hazir") { 
                            Box(modifier = Modifier.animateItemPlacement(androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow))) {
                                StatusHeader(if(isEn) "Ready" else if(isEn) "Ready" else "Hazir", hazirOrders.size, statusHazir) 
                            }
                        }
                        items(hazirOrders, key = { "r-${it.id}" }) { order ->
                            Box(modifier = Modifier.animateItemPlacement(androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow))) {
                                OrderCard(order = order, statusColor = statusHazir, onStatusChange = { viewModel.updateStatus(order.id, it) })
                            }
                        }
                    }

                    // HAZIRLANIYOR grubu (Hemen altinda, uzerinde calisilanlar)
                    if (hazirlaniyorOrders.isNotEmpty()) {
                        item(key = "header-hazirlaniyor") { 
                            Box(modifier = Modifier.animateItemPlacement(androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow))) {
                                StatusHeader(if(isEn) "Preparing" else "Hazirlaniyor", hazirlaniyorOrders.size, statusHazirlaniyor) 
                            }
                        }
                        items(hazirlaniyorOrders, key = { "h-${it.id}" }) { order ->
                            Box(modifier = Modifier.animateItemPlacement(androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow))) {
                                OrderCard(order = order, statusColor = statusHazirlaniyor, onStatusChange = { viewModel.updateStatus(order.id, it) })
                            }
                        }
                    }

                    // ALINDI grubu (Kuyruktaki yeni siparisler)
                    if (alindiOrders.isNotEmpty()) {
                        item(key = "header-alindi") { 
                            Box(modifier = Modifier.animateItemPlacement(androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow))) {
                                StatusHeader(if(isEn) "Received" else "Alindi", alindiOrders.size, statusAlindi) 
                            }
                        }
                        items(alindiOrders, key = { "a-${it.id}" }) { order ->
                            Box(modifier = Modifier.animateItemPlacement(androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow))) {
                                OrderCard(order = order, statusColor = statusAlindi, onStatusChange = { viewModel.updateStatus(order.id, it) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusHeader(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(50)))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = clrPrimary)
        Box(modifier = Modifier.background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
            Text("$count", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun OrderCard(
    order: AdminOrder,
    statusColor: Color,
    onStatusChange: (String) -> Unit
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = clrSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Sol renk serit (sitedeki border-left)
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(statusColor))
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                // Siparis no + Musteri Ismi
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("#${order.orderNo}", fontWeight = FontWeight.Bold, color = clrPrimary, fontSize = 15.sp)
                    Text(order.customerName, fontWeight = FontWeight.SemiBold, color = clrTextMuted, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Urunler
                Box(modifier = Modifier.fillMaxWidth().background(clrBg, RoundedCornerShape(8.dp)).padding(12.dp)) {
                    Column {
                        order.items.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(item.emoji, fontSize = 14.sp)
                                    Text(translateItem(item.name, isEn), fontSize = 14.sp, color = clrPrimary)
                                    Text("(${translateSize(item.sizeLabel, isEn)})", fontSize = 13.sp, color = Color(0xFF999999))
                                }
                                Text("x${item.qty}", fontWeight = FontWeight.Bold, color = clrAccent, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // Not
                if (!order.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF3CD), RoundedCornerShape(8.dp)).padding(10.dp)) {
                        Text("📝 ${order.note}", color = Color(0xFF856404), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Odeme + Tutar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(clrAccentLight, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        val payIcon = if (order.paymentMethod == "kart") "💳" else "💵"
                        val payLabel = if (order.paymentMethod == "kart") if(isEn) "Card" else "Kart" else if(isEn) "Cash" else "Nakit"
                        Text("$payIcon $payLabel", fontWeight = FontWeight.SemiBold, color = clrAccent, fontSize = 13.sp)
                    }
                    Text("₺${order.subtotalInt}", fontWeight = FontWeight.Bold, color = clrPrimary, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Aksiyon butonu
                when (translateStatus(order.status, isEn)) {
                    "alindi" -> ActionButton(if(isEn) "👨‍🍳 Start Preparing" else "👨‍🍳 Hazirlamaya Basla", statusHazirlaniyor) { onStatusChange("hazirlaniyor") }
                    "hazirlaniyor" -> ActionButton(if(isEn) "✅ Ready" else "✅ Hazir", statusHazir) { onStatusChange("hazir") }
                    "hazir" -> ActionButton(if(isEn) "🤝 Delivered" else "🤝 Teslim Edildi", statusTeslim) { onStatusChange("teslim_edildi") }
                    "teslim_edildi" -> Text("✓ Teslim edildi", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = statusTeslim, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

fun translateStatus(status: String, isEn: Boolean): String {
    if(!isEn) return status
    return when(status) {
        "Siparişiniz alındı" -> "Order received"
        "Hazırlanıyor" -> "Preparing"
        "Hazır" -> "Ready"
        if(isEn) "Delivered" else if(isEn) "Delivered" else "Teslim Edildi" -> "Delivered"
        "İptal Edildi" -> "Cancelled"
        else -> status
    }
}
fun translateSize(size: String, isEn: Boolean): String {
    if(!isEn) return size
    val translations = mapOf(
        "Tek" to "Single",
        "Çift" to "Double",
        "Sade" to "Plain",
        "Çikolatalı" to "Chocolate",
        "Normal" to "Normal",
        "Büyük" to "Large",
        "Dilim" to "Slice",
        "A la Mode" to "A la Mode",
        "3'lü" to "3-pack"
    )
    return translations[size] ?: size
}

fun translateSummary(summary: String, isEn: Boolean): String {
    if (!isEn) return summary
    var res = summary
    val translations = mapOf(
        "Türk Kahvesi" to "Turkish Coffee",
        "Lavanta Latte" to "Lavender Latte",
        "Sandviç" to "Sandwich",
        "Tek" to "Single",
        "Çift" to "Double",
        "Sade" to "Plain",
        "Çikolatalı" to "Chocolate",
        "Büyük" to "Large",
        "3'lü" to "3-pack",
        "Dilim" to "Slice"
    )
    for ((tr, en) in translations) {
        res = res.replace(tr, en)
    }
    return res
}

fun translateItem(name: String, isEn: Boolean): String {
    if(!isEn) return name
    val translations = mapOf(
        "Türk Kahvesi" to "Turkish Coffee",
        "Espresso" to "Espresso",
        "Cappuccino" to "Cappuccino",
        "Latte" to "Latte",
        "Americano" to "Americano",
        "Flat White" to "Flat White",
        "Iced Latte" to "Iced Latte",
        "Cold Brew" to "Cold Brew",
        "Frappuccino" to "Frappuccino",
        "Iced Americano" to "Iced Americano",
        "Caramel Macchiato" to "Caramel Macchiato",
        "Mocha" to "Mocha",
        "Lavanta Latte" to "Lavender Latte",
        "Matcha Latte" to "Matcha Latte",
        "Affogato" to "Affogato",
        "Tiramisu" to "Tiramisu",
        "Cheesecake" to "Cheesecake",
        "Brownie" to "Brownie",
        "Croissant" to "Croissant",
        "Sandviç" to "Sandwich",
        "Cookie" to "Cookie",
        
        "Tek" to "Single",
        "Çift" to "Double",
        "Dilim" to "Slice",
        "Sade" to "Plain",
        "Çikolatalı" to "Chocolate",
        "Normal" to "Regular",
        "Büyük" to "Large",
        "3'lü" to "3-Pack"
    )
    var res = name
    for ((tr, en) in translations) {
        res = res.replace(tr, en)
    }
    return res
}
