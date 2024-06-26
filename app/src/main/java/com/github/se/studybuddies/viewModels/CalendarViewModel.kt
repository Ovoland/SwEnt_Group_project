package com.github.se.studybuddies.viewModels

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.se.studybuddies.data.CalendarDataSource
import com.github.se.studybuddies.data.CalendarUiState
import com.github.se.studybuddies.data.DailyPlanner
import com.github.se.studybuddies.database.DatabaseConnection
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalendarViewModel(private val uid: String) : ViewModel() {

  private val dataSource by lazy { CalendarDataSource() }
  private val databaseConnection = DatabaseConnection()

  private val _dailyPlanners = mutableStateMapOf<String, MutableStateFlow<DailyPlanner>>()
  val dailyPlanners: Map<String, StateFlow<DailyPlanner>>
    get() = _dailyPlanners

  private val _uiState = MutableStateFlow(CalendarUiState.Init)
  val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      syncDailyPlannersWithFirebase()
      _uiState.update { currentState ->
        currentState.copy(dates = dataSource.getDates(currentState.yearMonth))
      }
    }
  }

  private suspend fun syncDailyPlannersWithFirebase() {
    val dailyPlanners = databaseConnection.getAllDailyPlanners(uid)
    val plannersMap = mutableMapOf<String, MutableStateFlow<DailyPlanner>>()
    dailyPlanners.forEach { planner -> plannersMap[planner.date] = MutableStateFlow(planner) }
    _dailyPlanners.clear()
    _dailyPlanners.putAll(plannersMap)
  }

  fun refreshDailyPlanners() {
    viewModelScope.launch { syncDailyPlannersWithFirebase() }
  }

  fun updateDailyPlanner(date: String, planner: DailyPlanner) {
    _dailyPlanners[date]?.value = planner

    viewModelScope.launch {
      val dailyPlanners = _dailyPlanners.values.map { it.value }.toList()
      databaseConnection.updateDailyPlanners(uid, dailyPlanners)
    }
  }

  fun getDailyPlanner(date: String): StateFlow<DailyPlanner> {
    return _dailyPlanners.getOrPut(date) {
      MutableStateFlow(DailyPlanner(date)).also { fetchAndUpdatePlanner(date) }
    }
  }

  private fun fetchAndUpdatePlanner(date: String) {
    viewModelScope.launch {
      val planner = databaseConnection.getDailyPlanner(uid, date)
      _dailyPlanners[date]?.value = planner
    }
  }

  fun toNextMonth(nextMonth: YearMonth) {
    viewModelScope.launch {
      _uiState.update { currentState ->
        currentState.copy(yearMonth = nextMonth, dates = dataSource.getDates(nextMonth))
      }
    }
  }

  fun toPreviousMonth(prevMonth: YearMonth) {
    viewModelScope.launch {
      _uiState.update { currentState ->
        currentState.copy(yearMonth = prevMonth, dates = dataSource.getDates(prevMonth))
      }
    }
  }

  fun deleteGoal(date: String, goal: String) {
    val planner = _dailyPlanners[date]?.value ?: return
    val updatedGoals = planner.goals.toMutableList().apply { remove(goal) }
    updateDailyPlanner(date, planner.copy(goals = updatedGoals))
  }

  fun deleteNote(date: String, note: String) {
    val planner = _dailyPlanners[date]?.value ?: return
    val updatedNotes = planner.notes.toMutableList().apply { remove(note) }
    updateDailyPlanner(date, planner.copy(notes = updatedNotes))
  }

  fun deleteAppointment(date: String, time: String) {
    val planner = _dailyPlanners[date]?.value ?: return
    val updatedAppointments = planner.appointments.toMutableMap().apply { remove(time) }
    updateDailyPlanner(date, planner.copy(appointments = updatedAppointments))
  }
}

class CalendarViewModelFactory(private val uid: String) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
      return CalendarViewModel(uid) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
