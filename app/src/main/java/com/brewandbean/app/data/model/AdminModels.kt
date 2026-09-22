package com.brewandbean.app.data.model

import com.google.gson.annotations.SerializedName

data class AdminOrder(
    val id: Int,
    @SerializedName("order_no") val orderNo: String,
    @SerializedName("customer_name") val customerName: String,
    val phone: String?,
    @SerializedName("table_no") val tableNo: Int,
    @SerializedName("payment_method") val paymentMethod: String,
    val status: String,
    val note: String?,
    val subtotal: String, // API string olarak donduruyor
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String?,
    val items: List<AdminOrderItem> = emptyList()
) {
    val subtotalInt: Int get() = subtotal.toDoubleOrNull()?.toInt() ?: 0
}

data class AdminOrderItem(
    val id: Int,
    @SerializedName("item_name") val name: String,
    val emoji: String,
    @SerializedName("size_label") val sizeLabel: String,
    val price: Int,
    val qty: Int
)

data class Report(
    val id: Int,
    @SerializedName("report_date") val reportDate: String,
    @SerializedName("total_orders") val totalOrders: Int,
    @SerializedName("total_revenue") val totalRevenue: String, // API string
    @SerializedName("total_items") val totalItems: Int,
    @SerializedName("card_revenue") val cardRevenue: String, // API string
    @SerializedName("cash_revenue") val cashRevenue: String, // API string
    @SerializedName("orders_data") val ordersData: ReportOrdersData? = null,
    @SerializedName("created_at") val createdAt: String? = null
) {
    val totalRevenueInt: Int get() = totalRevenue.toDoubleOrNull()?.toInt() ?: 0
    val cardRevenueInt: Int get() = cardRevenue.toDoubleOrNull()?.toInt() ?: 0
    val cashRevenueInt: Int get() = cashRevenue.toDoubleOrNull()?.toInt() ?: 0
}

data class ReportOrdersData(
    val orders: List<ReportOrder>? = null,
    @SerializedName("top_items") val topItems: Map<String, Int>? = null
)

data class ReportOrder(
    @SerializedName("order_no") val orderNo: String?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("table_no") val tableNo: Int?,
    @SerializedName("payment_method") val paymentMethod: String?,
    val status: String?,
    val subtotal: String?,
    @SerializedName("items_summary") val itemsSummary: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class ApiResponse(
    val success: Boolean?,
    val status: String?,
    val message: String?,
    val error: String?
)

data class EndOfDayResponse(
    val success: Boolean?,
    val report: EndOfDayReport? = null,
    val message: String?,
    val error: String?
)

data class EndOfDayReport(
    val date: String?,
    @SerializedName("total_orders") val totalOrders: Int?,
    @SerializedName("total_revenue") val totalRevenue: Double?,
    @SerializedName("total_items") val totalItems: Int?,
    @SerializedName("card_revenue") val cardRevenue: Double?,
    @SerializedName("cash_revenue") val cashRevenue: Double?,
    @SerializedName("top_items") val topItems: Map<String, Int>? = null
)

data class UpdateStatusRequest(
    @SerializedName("order_id") val orderId: Int,
    val status: String
)
