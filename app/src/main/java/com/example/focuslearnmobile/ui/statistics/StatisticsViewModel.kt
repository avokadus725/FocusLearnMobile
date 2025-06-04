// app/src/main/java/com/example/focuslearnmobile/ui/statistics/StatisticsViewModel.kt
package com.example.focuslearnmobile.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focuslearn.mobile.data.model.UserStatisticsDTO
import com.focuslearn.mobile.data.repository.FocusLearnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: FocusLearnRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    fun loadStatistics(period: StatisticsPeriod) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val (startDate, periodType) = getPeriodParameters(period)

                // Завантажуємо основну статистику
                val statisticsResult = repository.getUserStatistics(startDate, periodType)

                // Завантажуємо коефіцієнт продуктивності
                val productivityResult = repository.getProductivityCoefficient(startDate, periodType)

                // Завантажуємо найефективнішу методику
                val methodResult = repository.getMostEffectiveMethod(startDate, periodType)

                when (statisticsResult) {
                    is FocusLearnRepository.Result.Success -> {
                        val productivity = when (productivityResult) {
                            is FocusLearnRepository.Result.Success -> productivityResult.data.productivityCoefficient
                            else -> 0.0
                        }

                        val effectiveMethod = when (methodResult) {
                            is FocusLearnRepository.Result.Success -> methodResult.data.message
                            else -> ""
                        }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statistics = statisticsResult.data,
                            productivityCoefficient = productivity,
                            mostEffectiveMethod = effectiveMethod,
                            selectedPeriod = period
                        )
                    }

                    is FocusLearnRepository.Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = statisticsResult.message
                        )
                    }

                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Unknown error loading statistics"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading statistics: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun getPeriodParameters(period: StatisticsPeriod): Pair<String, String> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        return when (period) {
            StatisticsPeriod.DAILY -> {
                today.format(formatter) to "Day"
            }
            StatisticsPeriod.WEEKLY -> {
                today.minusDays(6).format(formatter) to "Week"
            }
            StatisticsPeriod.MONTHLY -> {
                today.minusDays(29).format(formatter) to "Month"
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val statistics: UserStatisticsDTO? = null,
    val productivityCoefficient: Double = 0.0,
    val mostEffectiveMethod: String = "",
    val selectedPeriod: StatisticsPeriod = StatisticsPeriod.DAILY
)