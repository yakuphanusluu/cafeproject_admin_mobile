package com.brewandbean.app.data.repository

import com.brewandbean.app.data.api.BrewBeanApi
import com.brewandbean.app.data.model.AdminOrder
import com.brewandbean.app.data.model.EndOfDayResponse
import com.brewandbean.app.data.model.Report
import com.brewandbean.app.data.model.UpdateStatusRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val api: BrewBeanApi
) {
    suspend fun getTodayOrders(): Result<List<AdminOrder>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getTodayOrders())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: Int, newStatus: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateOrderStatus(UpdateStatusRequest(orderId, newStatus))
            if (response.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.error ?: "Durum guncellenemedi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReports(): Result<List<Report>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getReports())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReportDetail(id: Int): Result<Report> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getReportDetail(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun endOfDay(): Result<EndOfDayResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.endOfDay()
            if (response.success == true) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.error ?: "Gunsonu alinamadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
