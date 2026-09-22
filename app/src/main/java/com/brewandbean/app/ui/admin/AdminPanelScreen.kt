package com.brewandbean.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.brewandbean.app.data.model.Report

// Sitedeki renkler birebir
private val clrPrimary = Color(0xFF1A1A2E)
private val clrAccent = Color(0xFFC8956C)
private val clrAccentLight = Color(0xFFF5EBE0)
private val clrBg = Color(0xFFF3F0EB)
private val clrSurface = Color(0xFFFFFFFF)
private val clrTextMuted = Color(0xFF7A7A7A)
private val clrBorder = Color(0xFFE8E0D8)
private val clrSuccess = Color(0xFF10B981)
private val clrDanger = Color(0xFFEF4444)

private val statusAlindi = Color(0xFF3B82F6)
private val statusHazirlaniyor = Color(0xFFF59E0B)
private val statusHazir = Color(0xFF10B981)
private val statusTeslim = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isReportsLoading by viewModel.isReportsLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEn by LanguageManager.isEnglish.collectAsState()

    // Sitedeki 2 tab: Dashboard + Gecmis Raporlar (admin.html .tabs)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Gunsonu onay dialog
    var showEndDayDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchReports()
    }

    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    // Gunsonu Onay Dialog (sitedeki #endDayModal)
    if (showEndDayDialog) {
        val totalOrders = orders.size
        val totalRevenue = orders.sumOf { it.subtotalInt }
        val cardRev = orders.filter { it.paymentMethod == "kart" }.sumOf { it.subtotalInt }
        val cashRev = totalRevenue - cardRev

        AlertDialog(
            onDismissRequest = { showEndDayDialog = false },
            title = { Text(if(isEn) "🌙 End of Day" else "🌙 Gunsonu Yap", fontWeight = FontWeight.Bold, color = clrPrimary) },
            text = {
                Column {
                    Text(
                        if(isEn) "This action will archive all order data for the day and reset active orders. This action cannot be undone." else "Bu islem gunun tum siparis verilerini arsivleyecek ve aktif siparisleri sifirlayacaktir. Bu islem geri alinamaz.",
                        color = clrTextMuted,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Sitedeki .eod-summary onizlemesi
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        EodStatMini("$totalOrders", if(isEn) "Orders" else "Siparis")
                        EodStatMini("₺$totalRevenue", if(isEn) "Revenue" else "Gelir")
                        EodStatMini("₺$cardRev", if(isEn) "💳 Card" else "💳 Kart")
                        EodStatMini("₺$cashRev", if(isEn) "💵 Cash" else "💵 Nakit")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEndDayDialog = false
                        viewModel.doEndOfDay()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = clrDanger)
                ) {
                    Text(if(isEn) "🌙 Confirm" else "🌙 Gunsonu Onayla", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDayDialog = false }) {
                    Text(if(isEn) "Cancel" else if(isEn) "Cancel" else "Iptal", color = clrTextMuted)
                }
            }
        )
    }

    Scaffold(
        containerColor = clrBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Sitedeki .topbar: koyu arka plan
            TopAppBar(
                title = {
                    Text(
                        "☕ Admin",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp,
                        maxLines = 1
                    )
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).background(clrSuccess, RoundedCornerShape(50)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if(isEn) "Live" else "Canli", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = clrPrimary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .background(clrPrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selectedTab == 0) clrAccent else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTab = 0 }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "📊 Dashboard",
                                color = if (selectedTab == 0) Color.White else clrPrimary.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selectedTab == 1) clrAccent else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTab = 1; viewModel.fetchReports() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                if(isEn) "📅 Reports" else "📅 Raporlar",
                                color = if (selectedTab == 1) Color.White else clrPrimary.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                if (selectedTab == 0) {
                    // ===== DASHBOARD TAB (sitedeki #tabDashboard) =====
                    DashboardContent(
                        orders = orders,
                        isLoading = isLoading,
                        onEndDay = { showEndDayDialog = true },
                        onRefresh = { viewModel.refreshOrders() }
                    )
                } else {
                    // ===== RAPORLAR TAB (sitedeki #tabReports) =====
                    ReportsContent(
                        reports = reports,
                        isLoading = isReportsLoading,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun EodStatMini(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = clrPrimary)
        Text(label, fontSize = 11.sp, color = clrTextMuted)
    }
}

/**
 * Sitedeki #tabDashboard icerigi:
 * - 4 istatistik karti (.stats-grid)
 * - Siparis tablosu (.table-wrap) + Gunsonu butonu
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun DashboardContent(
    orders: List<AdminOrder>,
    isLoading: Boolean,
    onEndDay: () -> Unit,
    onRefresh: () -> Unit
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    val totalOrders = orders.size
    val totalRevenue = orders.sumOf { it.subtotalInt }
    val cardRev = orders.filter { it.paymentMethod == "kart" }.sumOf { it.subtotalInt }
    val cashRev = totalRevenue - cardRev
    val avgOrder = if (totalOrders > 0) totalRevenue / totalOrders else 0
    val pending = orders.count { it.status != "teslim_edildi" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sitedeki .stats-grid: 4 kart (2x2)
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), "📦", "$totalOrders", if(isEn) "Total Orders" else "Toplam Siparis")
                StatCard(Modifier.weight(1f), "💰", "₺$totalRevenue", if(isEn) "Total Revenue" else "Toplam Gelir", if(isEn) "Card: ₺$cardRev | Cash: ₺$cashRev" else "Kart: ₺$cardRev | Nakit: ₺$cashRev")
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), "📊", "₺$avgOrder", if(isEn) "Average Order" else "Ortalama Siparis")
                StatCard(Modifier.weight(1f), "⏳", "$pending", if(isEn) "Pending Orders" else "Bekleyen Siparis")
            }
        }

        // Sitedeki .section-bar: baslik + gunsonu butonu
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if(isEn) "Today's Orders" else "Gunun Siparisleri", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = clrPrimary)
                // Sitedeki .end-day-btn
                Button(
                    onClick = onEndDay,
                    colors = ButtonDefaults.buttonColors(containerColor = clrDanger),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(if(isEn) "🌙 End of Day" else "🌙 Gunsonu Yap", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Sitedeki siparis tablosu — mobilde kart listesi olarak gosteriyoruz
        if (orders.isEmpty()) {
            item {
                Text(
                    text = if(isEn) "☕ No orders yet" else "☕ Henuz siparis yok",
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    textAlign = TextAlign.Center,
                    color = clrTextMuted,
                    fontSize = 15.sp
                )
            }
        } else {
            items(orders.sortedByDescending { it.id }, key = { it.id }) { order ->
                // Sitedeki tablo satirlari: No, Masa, Musteri, Urunler, Odeme, Tutar, Durum, Saat
                Box(modifier = Modifier.animateItemPlacement(androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow))) {
                    OrderTableCard(order)
                }
            }
        }
    }
}

/**
 * Sitedeki .stat-card birebir
 */
@Composable
private fun StatCard(modifier: Modifier, icon: String, value: String, label: String, subLabel: String? = null) {
    Card(
        modifier = modifier.height(160.dp),
        colors = CardDefaults.cardColors(containerColor = clrSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = clrPrimary, fontFamily = FontFamily.Serif)
            Text(label, fontSize = 12.sp, color = clrTextMuted, maxLines = 1)
            if (subLabel != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(subLabel, fontSize = 10.sp, color = clrAccent, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Sitedeki siparis tablosunun her bir satiri — mobilde kart formatinda
 * No | Masa | Musteri | Urunler | Odeme | Tutar | Durum | Saat
 */
@Composable
private fun OrderTableCard(order: AdminOrder) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    val statusLabels = mapOf(
        "alindi" to Pair(if(isEn) "⚪ Received" else "⚪ Alindi", statusAlindi),
        "hazirlaniyor" to Pair(if(isEn) "🟡 Preparing" else "🟡 Hazirlaniyor", statusHazirlaniyor),
        "hazir" to Pair(if(isEn) "🟢 Ready" else "🟢 Hazir", statusHazir),
        "teslim_edildi" to Pair(if(isEn) "⚫ Delivered" else "⚫ Teslim", statusTeslim)
    )
    val (statusLabel, statusColor) = statusLabels[order.status] ?: Pair(order.status, clrTextMuted)
    val payIcon = if (order.paymentMethod == "kart") "💳" else "💵"
    val payLabel = if (order.paymentMethod == "kart") if(isEn) "Card" else if(isEn) "Card" else "Kart" else if(isEn) "Cash" else if(isEn) "Cash" else "Nakit"
    val itemsSummary = order.items.joinToString(", ") { "${it.emoji} ${translateItem(it.name, isEn)} (${translateSize(it.sizeLabel, isEn)}) x${it.qty}" }
    val timeStr = try {
        order.createdAt.substring(11, 16)
    } catch (e: Exception) {
        ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = clrSurface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Ust satir: siparis no + durum + saat
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("#${order.orderNo}", fontWeight = FontWeight.Bold, color = clrPrimary, fontSize = 14.sp)
                // .status-badge
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(statusLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Musteri
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(order.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = clrPrimary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Urunler
            Text(itemsSummary, fontSize = 12.sp, color = clrTextMuted, maxLines = 2)
            Spacer(modifier = Modifier.height(8.dp))
            // Alt satir: odeme + tutar + saat
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // .payment-badge
                Box(
                    modifier = Modifier
                        .background(clrAccentLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("$payIcon $payLabel", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = clrAccent)
                }
                Text("₺${order.subtotal}", fontWeight = FontWeight.Bold, color = clrPrimary, fontSize = 14.sp)
                Text(timeStr, fontSize = 12.sp, color = clrTextMuted)
            }
        }
    }
}

/**
 * Sitedeki #tabReports icerigi: gecmis gunsonu raporlari listesi
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ReportsContent(
    reports: List<Report>,
    isLoading: Boolean,
    viewModel: AdminViewModel
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    val reportDetail by viewModel.reportDetail.collectAsState()
    var showDetailDialog by remember { mutableStateOf(false) }

    // Rapor Detayi Dialog (sitedeki #reportDetailModal)
    if (showDetailDialog && reportDetail != null) {
        val detail = reportDetail!!
        AlertDialog(
            onDismissRequest = { showDetailDialog = false; viewModel.clearReportDetail() },
            title = { Text(if(isEn) "📅 ${detail.reportDate} Report" else "📅 ${detail.reportDate} Raporu", fontWeight = FontWeight.Bold, color = clrPrimary, fontSize = 18.sp) },
            text = {
                LazyColumn {
                    item {
                        // Ozet Bilgiler
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            EodStatMini("${detail.totalOrders}", if(isEn) "Orders" else "Siparis")
                            EodStatMini("₺${detail.totalRevenueInt}", if(isEn) "Revenue" else "Gelir")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            EodStatMini("₺${detail.cardRevenueInt}", if(isEn) "💳 Card" else "💳 Kart")
                            EodStatMini("₺${detail.cashRevenueInt}", if(isEn) "💵 Cash" else "💵 Nakit")
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = clrBorder)
                        
                        // En Cok Satilanlar
                        detail.ordersData?.topItems?.let { topItems ->
                            if (topItems.isNotEmpty()) {
                                Text(if(isEn) "🏆 Best Sellers" else "🏆 En Cok Satilanlar", fontWeight = FontWeight.Bold, color = clrPrimary, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                topItems.forEach { (name, qty) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(translateItem(name, isEn), color = clrTextMuted, fontSize = 14.sp)
                                        Text(if(isEn) "$qty pcs" else "$qty adet", fontWeight = FontWeight.Bold, color = clrAccent, fontSize = 14.sp)
                                    }
                                }
                                Divider(modifier = Modifier.padding(vertical = 16.dp), color = clrBorder)
                            }
                        }
                        
                        // Tum Siparisler Listesi
                        detail.ordersData?.orders?.let { orders ->
                            if (orders.isNotEmpty()) {
                                Text(if(isEn) "📋 All Orders (${orders.size})" else "📋 Tum Siparisler (${orders.size})", fontWeight = FontWeight.Bold, color = clrPrimary, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    
                    detail.ordersData?.orders?.let { orders ->
                        items(orders) { o ->
                            val payIcon = if (o.paymentMethod == "kart") "💳" else "💵"
                            val time = try { o.createdAt?.substring(11, 16) ?: "" } catch(e:Exception) { "" }
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = clrBg),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("#${o.orderNo}", fontWeight = FontWeight.Bold, color = clrPrimary, fontSize = 13.sp)
                                        Text(time, fontSize = 12.sp, color = clrTextMuted)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text(o.customerName ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = clrPrimary)
                                    }
                                    if (!o.itemsSummary.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val translatedItems = translateSummary(o.itemsSummary ?: "", isEn)
                                        Text(translatedItems, fontSize = 12.sp, color = clrTextMuted, lineHeight = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(payIcon, fontSize = 13.sp)
                                        Text("₺${o.subtotal}", fontWeight = FontWeight.Bold, color = clrAccent, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailDialog = false; viewModel.clearReportDetail() }) {
                    Text(if(isEn) "Close" else "Kapat", color = clrPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && reports.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = clrAccent)
        } else if (reports.isEmpty()) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📊", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(if(isEn) "No end-of-day reports yet" else "Henuz gunsonu raporu yok", color = clrTextMuted, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(if(isEn) "Past End-of-Day Reports" else "Gecmis Gunsonu Raporlari", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = clrPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(reports, key = { it.id }) { report ->
                    Box(modifier = Modifier.animateItemPlacement(androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow))) {
                        ReportCard(
                            report = report,
                            onClick = { 
                                viewModel.fetchReportDetail(report.id)
                                showDetailDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sitedeki .report-card: tarih | siparis | gelir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportCard(report: Report, onClick: () -> Unit) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = clrSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tarih
            Text("📅 ${report.reportDate}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = clrPrimary)
            // Siparis sayisi
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${report.totalOrders}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = clrPrimary)
                Text(if(isEn) "Orders" else "Siparis", fontSize = 11.sp, color = clrTextMuted)
            }
            // Gelir
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("₺${report.totalRevenueInt}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = clrAccent)
                Text(if(isEn) "Revenue" else "Gelir", fontSize = 11.sp, color = clrTextMuted)
            }
            // Ok isareti
            Text("→", fontSize = 20.sp, color = clrTextMuted)
        }
    }
}
