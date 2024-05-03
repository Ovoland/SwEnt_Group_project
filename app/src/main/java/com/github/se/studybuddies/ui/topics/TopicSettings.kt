package com.github.se.studybuddies.ui.topics

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.studybuddies.R
import com.github.se.studybuddies.data.TopicItem
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.ui.GoBackRouteButton
import com.github.se.studybuddies.ui.Sub_title
import com.github.se.studybuddies.ui.TopNavigationBar
import com.github.se.studybuddies.viewModels.TopicViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TopicSettings(
    topicUID: String,
    topicViewModel: TopicViewModel,
    backRoute: String,
    navigationActions: NavigationActions
) {
  if (topicUID.isEmpty()) return
  topicViewModel.fetchTopicData(topicUID)
  val topicData by topicViewModel.topic.collectAsState()

  val nameState = remember { mutableStateOf(topicData.name) }

  topicData.let { nameState.value = it.name }
  var stagedDeletions by remember { mutableStateOf(setOf<String>()) }

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("account_settings"),
      topBar = {
        TopNavigationBar(
            title = { Sub_title(title = stringResource(R.string.profile_setting)) },
            navigationIcon = {
              GoBackRouteButton(navigationActions = navigationActions, backRoute)
            },
            actions = {})
      }) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Edit Topic Name")
              OutlinedTextField(
                  value = nameState.value,
                  onValueChange = { nameState.value = it },
                  singleLine = true,
                  colors =
                      OutlinedTextFieldDefaults.colors(
                          cursorColor = Color.Blue,
                          focusedBorderColor = Color.Blue,
                          unfocusedBorderColor = Color.Blue))
              Spacer(Modifier.height(20.dp))
              Text("Topic Items")
              topicData.exercises.plus(topicData.theory).forEach { item ->
                TopicItemRow(item, onDelete = { stagedDeletions = stagedDeletions.plus(item.uid) })
              }
              Button(
                  onClick = {
                    topicViewModel.updateTopicName(nameState.value)
                    topicViewModel.applyDeletions(stagedDeletions) {
                      stagedDeletions = setOf() // Reset staged deletions after application
                      Toast.makeText(
                              LocalContext.current,
                              "Changes saved successfully",
                              Toast.LENGTH_SHORT)
                          .show()
                    }
                  },
                  modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Save Changes")
                  }
            }
      }
}

@Composable
fun TopicItemRow(item: TopicItem, onDelete: () -> Unit) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(item.name, Modifier.weight(1f))
        IconButton(onClick = onDelete) {
          Icon(Icons.Filled.Delete, contentDescription = "Delete ${item.name}")
        }
      }
}
