package com.github.se.studybuddies.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.studybuddies.R
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.navigation.Route
import com.github.se.studybuddies.ui.groups.GroupFields
import com.github.se.studybuddies.ui.theme.Blue
import com.github.se.studybuddies.ui.theme.White
import com.github.se.studybuddies.viewModels.TopicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TopicCreaction(topicViewModel: TopicViewModel, navigationActions: NavigationActions) {
  val nameState = remember { mutableStateOf("") }

  Scaffold(
      modifier = Modifier
          .fillMaxSize()
          .background(Color.White).testTag("create_topic_scaffold"),
      topBar = {
        TopNavigationBar(
            title = { Sub_title("Create Topic") },
            navigationIcon = {
              GoBackRouteButton(navigationActions = navigationActions, Route.GROUPSHOME)
            },
            actions = {})
      }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(20.dp).testTag("create_topic_column"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Enter Topic Name")

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = nameState.value,
                onValueChange = { nameState.value = it },
                label = { Text(("Topic Name"), color = Blue) },
                placeholder = { Text(("Enter Topic Name"), color = Blue) },
                singleLine = true,
                modifier =
                Modifier.padding(0.dp)
                    .width(300.dp)
                    .height(65.dp)
                    .clip(MaterialTheme.shapes.small)
                    .testTag("topic_name"),
                colors =
                TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Blue, unfocusedBorderColor = Blue, cursorColor = Blue
                ))
              Spacer(modifier = Modifier.height(20.dp))

              Button(
                  onClick = {
                    topicViewModel.createTopic(nameState.value)
                    navigationActions.navigateTo(Route.GROUPSHOME)
                  }) {
                    Text("Save Topic")
                  }
            }
      }
}
