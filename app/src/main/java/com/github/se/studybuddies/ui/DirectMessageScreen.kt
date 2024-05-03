package com.github.se.studybuddies.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.github.se.studybuddies.R
import com.github.se.studybuddies.data.Chat
import com.github.se.studybuddies.data.ChatType
import com.github.se.studybuddies.data.User
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.navigation.Route
import com.github.se.studybuddies.viewModels.ChatViewModel
import com.github.se.studybuddies.viewModels.DirectMessageViewModel
import com.github.se.studybuddies.viewModels.MessageViewModel
import com.github.se.studybuddies.viewModels.UsersViewModel

@Composable
fun DirectMessageScreen(
    viewModel: DirectMessageViewModel,
    chatViewModel: ChatViewModel,
    usersViewModel: UsersViewModel,
    navigationActions: NavigationActions
) {
  val showAddPrivateMessageList = remember { mutableStateOf(false) }
  val chats = viewModel.directMessages.collectAsState(initial = emptyList())

  Log.d("MyPrint", "ListAllUsers called")
  chatViewModel
      .getChat()
      ?.let { MessageViewModel(it) }
      ?.let { ListAllUsers(it, showAddPrivateMessageList, usersViewModel) }

  Column {
    TopNavigationBar(
        title = { Text(text = stringResource(R.string.direct_messages_title)) },
        navigationIcon = {
          GoBackRouteButton(navigationActions = navigationActions, backRoute = Route.GROUPSHOME)
        },
        actions = {
          IconButton(
              onClick = {
                Log.d("MyPrint", "Add private message button clicked")
                if (showAddPrivateMessageList.value) {
                  showAddPrivateMessageList.value = false
                } else {
                  showAddPrivateMessageList.value = true
                }
              }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.new_private_message_icon))
              }
        })

    if (showAddPrivateMessageList.value) {
      ListAllUsers(
          MessageViewModel(Chat("uid", "name", "photoUrl", ChatType.PRIVATE, emptyList())),
          showAddPrivateMessageList,
          usersViewModel)
    } else {
      LazyColumn() {
        items(chats.value) { chat ->
          DirectMessageItem(chat) {
            chatViewModel.setChat(chat)
            navigationActions.navigateTo(Route.CHAT)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DirectMessageItem(chat: Chat, onClick: () -> Unit = {}) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth().padding(8.dp).combinedClickable(onClick = { onClick() })) {
        Image(
            painter = rememberImagePainter(chat.photoUrl),
            contentDescription = "User profile picture",
            modifier =
                Modifier.padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
                    .align(Alignment.CenterVertically)
                    .testTag("chat_user_profile_picture"),
            contentScale = ContentScale.Crop)
        Text(text = chat.name)
        Spacer(modifier = Modifier.weight(1f))
      }
}

@Composable
fun ListAllUsers(
    messageViewModel: MessageViewModel,
    showAddPrivateMessageList: MutableState<Boolean>,
    usersViewModel: UsersViewModel
) {
  val friendsData by usersViewModel.friends.collectAsState()
  val friends = remember { mutableStateOf(friendsData) }
  messageViewModel.currentUser.value?.let { usersViewModel.fetchAllFriends(it.uid) }

  Log.d("MyPrint", "ListAllUsers: ${friendsData.size}")
  Log.d("MyPrint", "ListAllUsers: ${friends.value.size}")
  LazyColumn(modifier = Modifier.fillMaxWidth()) {
    items(friendsData) { UserItem(it, messageViewModel, showAddPrivateMessageList) }
  }
  Text(text = "ListAllUsers: ${friendsData.size}")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserItem(
    user: User,
    messageViewModel: MessageViewModel,
    showAddPrivateMessageList: MutableState<Boolean>
) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
          Modifier.fillMaxWidth()
              .padding(8.dp)
              .combinedClickable(
                  onClick = {
                    messageViewModel.startDirectMessage(user.uid)
                    showAddPrivateMessageList.value = false
                  })) {
        Image(
            painter = rememberImagePainter(user.photoUrl),
            contentDescription = stringResource(R.string.contentDescription_user_profile_picture),
            modifier =
                Modifier.padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
                    .align(Alignment.CenterVertically)
                    .testTag("chat_user_profile_picture"),
            contentScale = ContentScale.Crop)
        Text(text = user.username)
      }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun ListAllUsersPreview() {
  val viewModel = MessageViewModel(Chat("uid", "name", "photoUrl", ChatType.PRIVATE, emptyList()))
  ListAllUsers(viewModel, mutableStateOf(true), UsersViewModel())
}
