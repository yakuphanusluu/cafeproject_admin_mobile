package com.brewandbean.app.data.api

import com.brewandbean.app.data.model.AdminOrder
import com.brewandbean.app.data.model.ApiResponse
import com.brewandbean.app.data.model.EndOfDayResponse
import com.brewandbean.app.data.model.Report
import com.brewandbean.app.data.model.UpdateStatusRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BrewBeanApi {
    @GET("orders.php")
    suspend fun getTodayOrders(): List<AdminOrder>

    @POST("status.php")
    suspend fun updateOrderStatus(@Body request: UpdateStatusRequest): ApiResponse

    @GET("reports.php")
    suspend fun getReports(): List<Report>

    @GET("reports.php")
    suspend fun getReportDetail(@Query("id") id: Int): Report

    @POST("end-of-day.php")
    suspend fun endOfDay(): EndOfDayResponse
}
