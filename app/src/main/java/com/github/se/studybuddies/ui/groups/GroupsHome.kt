package com.github.se.studybuddies.ui.groups

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.github.se.studybuddies.R
import com.github.se.studybuddies.data.Group
import com.github.se.studybuddies.database.DatabaseConnection
import com.github.se.studybuddies.navigation.GROUPS_SETTINGS_DESTINATIONS
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.navigation.Route
import com.github.se.studybuddies.ui.MainScreenScaffold
import com.github.se.studybuddies.ui.SearchIcon
import com.github.se.studybuddies.ui.theme.Blue
import com.github.se.studybuddies.ui.screens.MainScreenScaffold
import com.github.se.studybuddies.ui.screens.SearchIcon
import com.github.se.studybuddies.ui.theme.White
import com.github.se.studybuddies.viewModels.GroupsHomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val db = DatabaseConnection()

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GroupsHome(
    uid: String,
    groupsHomeViewModel: GroupsHomeViewModel,
    navigationActions: NavigationActions
) {
  val coroutineScope = rememberCoroutineScope()
  groupsHomeViewModel.fetchGroups(uid)
  val groups by groupsHomeViewModel.groups.observeAsState()
  val groupList = remember { mutableStateOf(groups?.getAllTasks() ?: emptyList()) }
  var isLoading by remember { mutableStateOf(true) }

  groups?.let {
    groupList.value = it.getAllTasks()
    coroutineScope.launch {
      delay(1500L) // delay for 1 second
      isLoading = false
    }
  }

  MainScreenScaffold(
      navigationActions,
      Route.GROUPSHOME,
      content = { innerPadding ->
        if (isLoading) {
          Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
          }
        } else if (groupList.value.isEmpty()) {
          Column(
              modifier = Modifier.fillMaxSize().testTag("GroupsHome"),
              horizontalAlignment = Alignment.Start,
              verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
          ) {
            Spacer(modifier = Modifier.height(80.dp))
            Text(stringResource(R.string.join_or_create_a_new_group), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(80.dp))
            AddGroupButton(navigationActions = navigationActions)
            AddLinkButton(navigationActions = navigationActions)
          }
        } else {
          Column(
              modifier = Modifier.fillMaxSize().testTag("GroupsHome"),
              horizontalAlignment = Alignment.Start,
              verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
          ) {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
                content = {
                  items(groupList.value) { group -> GroupItem(group, navigationActions) }
                  item { AddGroupButton(navigationActions) }
                  item { AddLinkButton(navigationActions) }
                })
          }
        }
      },
      title = stringResource(R.string.groups),
      iconOptions = { SearchIcon() })
}

@Composable
fun GroupsSettingsButton(groupUID: String, navigationActions: NavigationActions) {
  val expandedState = remember { mutableStateOf(false) }
  IconButton(
      onClick = { expandedState.value = true },
  ) {
    Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = stringResource(R.string.dots_menu))
  }
  DropdownMenu(expanded = expandedState.value, onDismissRequest = { expandedState.value = false }) {
    GROUPS_SETTINGS_DESTINATIONS.forEach { item ->
      DropdownMenuItem(
          onClick = {
            expandedState.value = false
            navigationActions.navigateTo("${item.route}/$groupUID")
          }) {
            Spacer(modifier = Modifier.size(16.dp))
            Text(item.textId)
          }
    }
  }
}

@Composable
fun GroupItem(group: Group, navigationActions: NavigationActions) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .background(Color.White)
              .clickable {
                val groupUid = group.uid
                Log.d("GROUPEDITCLICK", "Tapped on group")
                navigationActions.navigateTo("${Route.GROUP}/$groupUid")
              }
              .drawBehind {
                val strokeWidth = 1f
                val y = size.height - strokeWidth / 2
                drawLine(Color.LightGray, Offset(0f, y), Offset(size.width, y), strokeWidth)
              }) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
          Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.Transparent)) {
            Image(
                painter = rememberImagePainter(group.picture),
                contentDescription = stringResource(id = R.string.group_picture),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop)
          }
          Spacer(modifier = Modifier.size(16.dp))
          Text(
              text = group.name,
              modifier = Modifier.align(Alignment.CenterVertically),
              style = TextStyle(fontSize = 20.sp),
              lineHeight = 28.sp)
          Spacer(modifier = Modifier.weight(1f))
          GroupsSettingsButton(group.uid, navigationActions)
        }
      }
}

@Composable
fun AddGroupButton(navigationActions: NavigationActions) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.End) {
        Button(
            onClick = { navigationActions.navigateTo(Route.CREATEGROUP) },
            modifier = Modifier.width(64.dp).height(64.dp).clip(MaterialTheme.shapes.medium)) {
              Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = stringResource(R.string.create_a_task),
                  tint = White)
            }
      }
}

@Composable
fun AddLinkButton(navigationActions: NavigationActions) {
  var text by remember { mutableStateOf("") }
  var isTextFieldVisible by remember { mutableStateOf(false) }
  var showError by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  var showSucces by remember { mutableStateOf(false) }

  Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.End) {
        Button(
            onClick = { isTextFieldVisible = !isTextFieldVisible },
            modifier = Modifier.width(64.dp).height(64.dp).clip(MaterialTheme.shapes.medium)) {
              Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = stringResource(R.string.link_button),
                  tint = White)
            }
      }
  if (isTextFieldVisible) {
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(stringResource(R.string.enter_link)) },
        modifier = Modifier.fillMaxSize().padding(16.dp),
        singleLine = true,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                unfocusedLabelColor = Blue,
                unfocusedIndicatorColor = Blue,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = {
                  isTextFieldVisible = false
                  // add user to groups
                  val groupUID = text.substringAfterLast("/")
                  scope.launch {
                    val error = db.addUserToGroup(groupUID)
                    if (error == -1) {
                      showError = true
                      delay(3000L) // delay for 3 seconds
                      showError = false
                    } else {
                      showSucces = true
                      delay(1500L) // delay for 1.5 seconds
                      showSucces = false
                      navigationActions.navigateTo("${Route.GROUP}/$groupUID")
                    }
                  }
                }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done))
  }

  if (showError) {
    Snackbar(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        action = { TextButton(onClick = { showError = false }) {} }) {
          Text(stringResource(R.string.the_link_entered_is_invalid))
        }
  }
  if (showSucces) {
    Snackbar(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        action = { TextButton(onClick = { showSucces = false }) {} }) {
          Text(stringResource(R.string.you_have_been_successfully_added_to_the_group))
        }
  }
}

@Composable
fun AddGroup(navigationActions: NavigationActions) {
  Button(
      onClick = { navigationActions.navigateTo(Route.CREATEGROUP) },
      modifier = Modifier.width(64.dp).height(64.dp).clip(MaterialTheme.shapes.medium)) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(id = R.string.create_a_task),
            tint = White)
      }
}
