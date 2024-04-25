package com.github.se.studybuddies.ui.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.studybuddies.data.Location
import com.github.se.studybuddies.data.todo.ToDo
import com.github.se.studybuddies.data.todo.ToDoStatus
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.viewModels.ToDoViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun CreateToDo(todoViewModel: ToDoViewModel, navigationActions: NavigationActions) {
  val titleState = remember { mutableStateOf("") }
  val descriptionState = remember { mutableStateOf("") }
  val assigneeState = remember { mutableStateOf("") }
  val locationState = remember { mutableStateOf(Location(0.0, 0.0, "")) }
  val onLocationChanged: (Location) -> Unit = { newLocation -> locationState.value = newLocation }
  val locationQuery = remember { mutableStateOf("") }

  val selectedDate = remember { mutableStateOf(LocalDate.now()) }
  val isOpen = remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxSize().testTag("createScreen")) {
    TodoTopBar(navigationActions, "Create a new task")
    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally) {
          item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                  TodoFields(
                      titleState,
                      descriptionState,
                      assigneeState,
                      onLocationChanged,
                      locationQuery,
                      selectedDate,
                      isOpen)
                  TodoSaveButton(titleState) {
                    val newTodo =
                        ToDo(
                            uid = "",
                            name = titleState.value,
                            description = descriptionState.value,
                            assigneeName = assigneeState.value,
                            dueDate = selectedDate.value,
                            location = locationState.value,
                            status = ToDoStatus.CREATED)
                    todoViewModel.addNewTodo(newTodo)
                    navigationActions.goBack()
                  }
                }
          }
        }
  }
  if (isOpen.value) {
    CustomDatePickerDialog(
        onAccept = {
          isOpen.value = false
          if (it != null) {
            selectedDate.value = Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
          }
        },
        onCancel = { isOpen.value = false })
  }
}
