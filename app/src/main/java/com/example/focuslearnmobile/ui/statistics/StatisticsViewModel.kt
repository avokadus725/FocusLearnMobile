// app/src/main/java/com/example/focuslearnmobile/ui/statistics/StatisticsViewModel.kt
package com.example.focuslearnmobile.ui.statistics

import android.os.Build
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadStatistics(period: StatisticsPeriod) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val (startDate, periodType) = getPeriodParameters(period)

                println("StatisticsViewModel: Loading stats for period $startDate, type $periodType")

                // Завантажуємо основну статистику
                val statisticsResult = repository.getUserStatistics(startDate, periodType)

                println("StatisticsViewModel: Statistics result type: ${statisticsResult::class.simpleName}")

                // Завантажуємо коефіцієнт продуктивності
                val productivityResult = repository.getProductivityCoefficient(startDate, periodType)

                // Завантажуємо найефективнішу методику
                val methodResult = repository.getMostEffectiveMethod(startDate, periodType)

                when (statisticsResult) {
                    is FocusLearnRepository.Result.Success -> {
                        println("StatisticsViewModel: Statistics loaded successfully: ${statisticsResult.data}")

                        val productivity = when (productivityResult) {
                            is FocusLearnRepository.Result.Success -> {
                                println("StatisticsViewModel: Productivity: ${productivityResult.data.productivityCoefficient}")
                                productivityResult.data.productivityCoefficient
                            }
                            is FocusLearnRepository.Result.Error -> {
                                println("StatisticsViewModel: Productivity error: ${productivityResult.message}")
                                0.0
                            }
                            else -> 0.0
                        }

                        val effectiveMethod = when (methodResult) {
                            is FocusLearnRepository.Result.Success -> {
                                println("StatisticsViewModel: Method: ${methodResult.data.message}")
                                methodResult.data.message
                            }
                            is FocusLearnRepository.Result.Error -> {
                                println("StatisticsViewModel: Method error: ${methodResult.message}")
                                ""
                            }
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
                        println("StatisticsViewModel: Statistics error: ${statisticsResult.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = statisticsResult.message
                        )
                    }

                    else -> {
                        println("StatisticsViewModel: Unknown statistics result")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Unknown error loading statistics"
                        )
                    }
                }

            } catch (e: Exception) {
                println("StatisticsViewModel: Exception: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading statistics: ${e.localizedMessage}"
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPeriodParameters(period: StatisticsPeriod): Pair<String, String> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        return when (period) {
            StatisticsPeriod.DAILY -> {
                today.format(formatter) to "Day"  // Сьогоднішня дата
            }
            StatisticsPeriod.WEEKLY -> {
                today.minusDays(6).format(formatter) to "Week"  // Початок тижня
            }
            StatisticsPeriod.MONTHLY -> {
                today.minusDays(29).format(formatter) to "Month"  // Початок місяця
            }
        }
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