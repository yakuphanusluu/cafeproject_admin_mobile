package com.brewandbean.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewandbean.app.data.model.AdminOrder
import com.brewandbean.app.data.model.EndOfDayReport
import com.brewandbean.app.data.model.Report
import com.brewandbean.app.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<AdminOrder>>(emptyList())
    val orders: StateFlow<List<AdminOrder>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _isReportsLoading = MutableStateFlow(false)
    val isReportsLoading: StateFlow<Boolean> = _isReportsLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Gunsonu sonuc raporu
    private val _endOfDayResult = MutableStateFlow<EndOfDayReport?>(null)
    val endOfDayResult: StateFlow<EndOfDayReport?> = _endOfDayResult.asStateFlow()

    // Rapor detayi
    private val _reportDetail = MutableStateFlow<Report?>(null)
    val reportDetail: StateFlow<Report?> = _reportDetail.asStateFlow()

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                fetchOrders(showLoading = _orders.value.isEmpty())
                delay(5000) // 5 saniyede bir yenile
            }
        }
    }

    fun refreshOrders() {
        viewModelScope.launch {
            fetchOrders(showLoading = true)
        }
    }

    private suspend fun fetchOrders(showLoading: Boolean) {
        if (showLoading) _isLoading.value = true
        
        repository.getTodayOrders()
            .onSuccess {
                _orders.value = it
                _errorMessage.value = null
            }
            .onFailure {
                _errorMessage.value = it.message ?: "Siparisler alinamadi"
            }
        
        if (showLoading) _isLoading.value = false
    }

    fun updateStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            val currentOrders = _orders.value
            // Optimistic update
            _orders.value = currentOrders.map { 
                if (it.id == orderId) it.copy(status = newStatus) else it 
            }

            repository.updateOrderStatus(orderId, newStatus)
                .onFailure {
                    _orders.value = currentOrders // Revert
                    _errorMessage.value = it.message ?: "Durum guncellenemedi"
                }
        }
    }

    fun clearError() { _errorMessage.value = null }
    fun clearSuccess() { _successMessage.value = null }
    fun clearEndOfDayResult() { _endOfDayResult.value = null }
    fun clearReportDetail() { _reportDetail.value = null }

    fun fetchReports() {
        viewModelScope.launch {
            _isReportsLoading.value = true
            repository.getReports()
                .onSuccess { _reports.value = it }
                .onFailure { _errorMessage.value = it.message ?: "Raporlar alinamadi" }
            _isReportsLoading.value = false
        }
    }

    fun fetchReportDetail(reportId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getReportDetail(reportId)
                .onSuccess { _reportDetail.value = it }
                .onFailure { _errorMessage.value = it.message ?: "Rapor detayi alinamadi" }
            _isLoading.value = false
        }
    }

        private fun getLocalizedMessage(msg: String?): String {
        if (msg == null) return ""
        if (!com.brewandbean.app.util.LanguageManager.isEnglish.value) return msg
        if (msg.contains("oluşturuldu") || msg.contains("arsivlendi") || msg.contains("arşivlendi") || msg.contains("olusturuldu")) {
            return "End-of-day report generated and orders archived"
        }
        return msg
    }

    fun doEndOfDay() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.endOfDay()
                .onSuccess { response ->
                    _endOfDayResult.value = response.report
                    _successMessage.value = getLocalizedMessage(response.message ?: "Gunsonu basariyla alindi")
                    refreshOrders()
                    fetchReports()
                }
                .onFailure {
                    if (it.message?.contains("409") == true) {
                        val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.value
                        _errorMessage.value = if(isEn) "An end-of-day report has already been generated for today!" else "Bugun icin zaten bir gunsonu raporu alinmis!"
                    } else {
                        _errorMessage.value = it.message ?: "Gunsonu alinamadi"
                    }
                }
            _isLoading.value = false
        }
    }
}
