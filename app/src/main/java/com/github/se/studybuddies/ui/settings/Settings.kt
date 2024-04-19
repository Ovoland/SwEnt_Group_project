package com.github.se.studybuddies.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.ui.GoBackRouteButton
import com.github.se.studybuddies.ui.Main_title
import com.github.se.studybuddies.ui.TopNavigationBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(backRoute: String, navigationActions: NavigationActions) {
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        TopNavigationBar(
            title = { Main_title(title = "Settings") },
            navigationIcon = {
              GoBackRouteButton(navigationActions = navigationActions, backRoute = backRoute)
            },
            actions = {})
      }) {
        Text("Settings")
      }
}
