package com.github.se.studybuddies.ui.chat

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.github.se.studybuddies.R
import com.github.se.studybuddies.data.User
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.navigation.Route
import com.github.se.studybuddies.ui.shared_elements.MainScreenScaffold
import com.github.se.studybuddies.ui.theme.Blue
import com.github.se.studybuddies.ui.theme.Green
import com.github.se.studybuddies.ui.theme.LightBlue
import com.github.se.studybuddies.ui.theme.White
import com.github.se.studybuddies.viewModels.ContactsViewModel
import com.github.se.studybuddies.viewModels.DirectMessagesViewModel

@Composable
fun ContactListScreen(
    currentUID: String,
    navigationActions: NavigationActions,
    contactsViewModel: ContactsViewModel,
    directMessagesViewModel: DirectMessagesViewModel
) {

  contactsViewModel.fetchAllContacts(currentUID)
  contactsViewModel.fetchAllRequests(currentUID)
  contactsViewModel.fetchAllFriends(currentUID)

  val showAddPrivateMessageList = remember { mutableStateOf(false) }

  val contacts = contactsViewModel.contacts.collectAsState().value
  val contactList = contacts.getAllTasks()

  Log.d("ContactListScreen", "contactlist is $contactList")

  val friends = contactsViewModel.friends.collectAsState().value
  // val friendList = friends.getAllTasks()
  // Log.d("ContactListScreen", "friendlist is ${friendList}")

  val requests by contactsViewModel.requests.collectAsState()
  val requestList = remember { mutableStateOf(requests.getAllTasks() ?: emptyList()) }

  MainScreenScaffold(
      navigationActions = navigationActions,
      backRoute = Route.DIRECT_MESSAGE,
      content = { innerPadding ->
        if (showAddPrivateMessageList.value) {
          Box(
              modifier =
                  Modifier.fillMaxSize().padding(innerPadding).testTag("add_private_message")) {
                ListAllUsers(showAddPrivateMessageList, directMessagesViewModel, contactsViewModel)
              }
        } else {
          Column(
              modifier =
                  Modifier.padding(top = 63.dp)
                      .padding(bottom = 100.dp)
                      .fillMaxSize()
                      .testTag("direct_messages_not_empty"),
              horizontalAlignment = Alignment.Start,
              verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
          ) {
            if (requestList.value.isEmpty() && contactList.isEmpty()) {
              Log.d("MyPrint", "Contact and Request list is empty")
              Text(
                  modifier =
                      Modifier.fillMaxSize().padding(innerPadding).testTag("direct_messages_empty"),
                  text = stringResource(R.string.direct_messages_empty))
            } else {
              Log.d("MyPrint", "Request or contact list is not empty")
              LazyColumn(
                  modifier =
                      Modifier.fillMaxWidth()
                          .fillMaxHeight(0.452f)
                          .background(LightBlue)
                          .testTag("request_list")) {
                    items(requestList.value) { request -> RequestItem(request, contactsViewModel) }
                    items(1) { Divider(thickness = 2.dp, color = Blue) }
                    items(contactList) { contact ->
                      val friendID = contact.getOtherUser(currentUID)
                      Log.d("ContactListScreen", "otheruser id is $friendID")
                      val filteredFriend = friends.getFilteredFriends(friendID)
                      var friend = User.empty()
                      if (filteredFriend.isNotEmpty()) {
                        friend = filteredFriend.get(0)
                      }
                      val hasDM = contact.hasStartedDM
                      ContactItem(friend, hasDM) {
                        if (!hasDM) {
                          directMessagesViewModel.startDirectMessage(friendID, contact.id)
                          navigationActions.navigateTo(Route.DIRECT_MESSAGE)
                          contactsViewModel.updateContactHasDM(contact.id, true)
                        } else {
                          navigationActions.navigateTo("${Route.CONTACT_SETTINGS}/${contact.id}")
                        }
                      }
                    }
                  }
            }
          }
        }

        Box(
            contentAlignment = Alignment.BottomEnd, // Aligns the button to the bottom end (right)
            modifier =
                Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
              GoToMessages(navigationActions)
            }
        Box(
            contentAlignment = Alignment.BottomStart, // Aligns the button to the bottom end (right)
            modifier =
                Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
              AddNewPrivateMessage(showAddPrivateMessageList)
            }
      },
      title =
          if (showAddPrivateMessageList.value) stringResource(R.string.start_direct_message_title)
          else stringResource(R.string.contact_list),
      iconOptions = {})
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactItem(friend: User, hasDM: Boolean, onClick: () -> Unit = {}) {

  /*
  val userVM = UserViewModel()
  val friendData by userVM.userData.observeAsState()
  userVM.fetchUserData(friendID)
  val nameState = remember { mutableStateOf(friendData?.username ?: "") }
  val photoState = remember { mutableStateOf(friendData?.photoUrl ?: Uri.EMPTY) }

  friendData?.let {
      nameState.value = it.username
      photoState.value = it.photoUrl
  }

   */

  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
          Modifier.fillMaxWidth()
              .background(color = White)
              .border(color = LightBlue, width = Dp.Hairline)
              .padding(8.dp)
              .combinedClickable(onClick = onClick)
              .testTag("chat_item")) {
        Image(
            painter = rememberAsyncImagePainter(friend.photoUrl),
            contentDescription = stringResource(R.string.contentDescription_user_profile_picture),
            modifier =
                Modifier.padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
                    .align(Alignment.CenterVertically)
                    .testTag("chat_user_profile_picture"),
            contentScale = ContentScale.Crop)
        Text(text = friend.username, modifier = Modifier.testTag("chat_name"))
        Spacer(modifier = Modifier.size(10.dp))
        if (!hasDM) {
          Text(
              text = "Click to start a conversation",
              modifier = Modifier.testTag("chat_name"),
              color = Blue)
        }
      }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RequestItem(request: User, contactsViewModel: ContactsViewModel) {
  Column(
      Modifier.fillMaxWidth()
          .background(color = White)
          .border(color = LightBlue, width = Dp.Hairline)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth().background(color = White).testTag("chat_item")) {
              Image(
                  painter = rememberAsyncImagePainter(request.photoUrl),
                  contentDescription =
                      stringResource(R.string.contentDescription_user_profile_picture),
                  modifier =
                      Modifier.padding(8.dp)
                          .size(40.dp)
                          .clip(CircleShape)
                          .border(2.dp, Color.Gray, CircleShape)
                          .align(Alignment.CenterVertically)
                          .testTag("chat_user_profile_picture"),
                  contentScale = ContentScale.Crop)
              Text(
                  text = "${request.username} sent you a friend request",
                  modifier = Modifier.fillMaxWidth().testTag("chat_name"),
                  maxLines = 1)
            }
        Row(
            verticalAlignment = Alignment.Top,
            modifier =
                Modifier.fillMaxWidth()
                    .background(color = White)
                    .padding(bottom = 12.dp)
                    .testTag("chat_item"),
            horizontalArrangement = Arrangement.Center,
        ) {
          Button(
              onClick = { contactsViewModel.acceptRequest(request.uid) },
              colors =
                  ButtonColors(
                      Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent),
              modifier =
                  Modifier.clip(MaterialTheme.shapes.medium)
                      .background(color = Green)
                      .width(100.dp)
                      .height(32.dp)
                      .testTag("add_private_message_button")) {
                Text("Accept", color = White)
              }
          Spacer(modifier = Modifier.size(30.dp))
          Button(
              onClick = { contactsViewModel.dismissRequest(request.uid) },
              colors =
                  ButtonColors(
                      Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent),
              modifier =
                  Modifier.clip(MaterialTheme.shapes.medium)
                      .background(color = Color.Red)
                      .width(100.dp)
                      .height(32.dp)
                      .testTag("add_private_message_button")) {
                Text(
                    "Deny",
                    color = White,
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center)
              }
        }
      }
}

@Composable
fun GoToMessages(navigationActions: NavigationActions) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.End) {
        IconButton(
            onClick = { navigationActions.navigateTo(Route.DIRECT_MESSAGE) },
            modifier =
                Modifier.width(64.dp)
                    .height(64.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(color = Blue)
                    .testTag("add_private_message_button")) {
              Icon(
                  painterResource(id = R.drawable.messages),
                  contentDescription = stringResource(R.string.contentDescription_icon_messages),
                  modifier = Modifier.size(40.dp),
                  tint = White)
            }
      }
}
