package com.github.se.studybuddies.ui.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.github.se.studybuddies.R
import com.github.se.studybuddies.data.Chat
import com.github.se.studybuddies.data.ChatType
import com.github.se.studybuddies.data.Message
import com.github.se.studybuddies.data.MessageVal
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.navigation.Route
import com.github.se.studybuddies.permissions.checkPermission
import com.github.se.studybuddies.permissions.imagePermissionVersion
import com.github.se.studybuddies.ui.shared_elements.SaveButton
import com.github.se.studybuddies.ui.shared_elements.SecondaryTopBar
import com.github.se.studybuddies.ui.shared_elements.SetPicture
import com.github.se.studybuddies.ui.theme.Blue
import com.github.se.studybuddies.ui.theme.LightBlue
import com.github.se.studybuddies.viewModels.DirectMessageViewModel
import com.github.se.studybuddies.viewModels.MessageViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(viewModel: MessageViewModel, navigationActions: NavigationActions) {
  val messages = viewModel.messages.collectAsState(initial = emptyList()).value
  val showOptionsDialog = remember { mutableStateOf(false) }
  val showEditDialog = remember { mutableStateOf(false) }
  val showIconsOptions = remember { mutableStateOf(false) }
  val showAddImage = remember { mutableStateOf(false) }
  val showAddLink = remember { mutableStateOf(false) }
  val showAddFile = remember { mutableStateOf(false) }

  var selectedMessage by remember { mutableStateOf<Message?>(null) }
  val listState = rememberLazyListState()

  LaunchedEffect(messages) {
    if (messages.isNotEmpty()) {
      listState.scrollToItem(messages.lastIndex)
    }
  }

  selectedMessage?.let {
    OptionsDialog(viewModel, it, showOptionsDialog, showEditDialog, navigationActions)
  }
  selectedMessage?.let { EditDialog(viewModel, it, showEditDialog) }

  IconsOptionsList(showIconsOptions, showAddImage, showAddLink, showAddFile)

  SendPhotoMessage(viewModel, showAddImage)
  SendLinkMessage(viewModel, showAddLink)
  SendFileMessage(viewModel, showAddFile)

  Column(
      modifier =
          Modifier.fillMaxSize()
              .background(LightBlue)
              .navigationBarsPadding()
              .testTag("chat_screen")) {
        SecondaryTopBar(onClick = { navigationActions.goBack() }) {
          when (viewModel.chat.type) {
            ChatType.GROUP,
            ChatType.TOPIC -> ChatGroupTitle(viewModel.chat)
            ChatType.PRIVATE -> PrivateChatTitle(viewModel.chat)
          }
        }
        LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(8.dp)) {
          items(messages) { message ->
            val isCurrentUserMessageSender = viewModel.isUserMessageSender(message)
            val displayName = viewModel.chat.type != ChatType.PRIVATE && !isCurrentUserMessageSender
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(2.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                              selectedMessage = message
                              showOptionsDialog.value = true
                            })
                        .testTag("chat_message_row"),
                horizontalArrangement =
                    if (isCurrentUserMessageSender) {
                      Arrangement.End
                    } else {
                      Arrangement.Start
                    }) {
                  TextBubble(
                      message,
                      displayName,
                  )
                }
          }
        }
        MessageTextFields(
            onSend = { viewModel.sendTextMessage(it) }, showIconsOptions = showIconsOptions)
      }
}

@Composable
fun TextBubble(message: Message, displayName: Boolean = false) {
  val browserLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle the result if needed
          }
  val context = LocalContext.current

  Row(modifier = Modifier.padding(1.dp).testTag("chat_text_bubble")) {
    if (displayName) {
      Image(
          painter = rememberAsyncImagePainter(message.sender.photoUrl.toString()),
          contentDescription = stringResource(R.string.contentDescription_user_profile_picture),
          modifier =
              Modifier.size(40.dp)
                  .clip(CircleShape)
                  .border(2.dp, Gray, CircleShape)
                  .align(Alignment.CenterVertically)
                  .testTag("chat_user_profile_picture"),
          contentScale = ContentScale.Crop)

      Spacer(modifier = Modifier.width(8.dp))
    }

    Box(
        modifier =
            Modifier.background(White, RoundedCornerShape(20.dp))
                .padding(1.dp)
                .testTag("chat_text_bubble_box")) {
          Column(modifier = Modifier.padding(8.dp)) {
            if (displayName) {
              Text(
                  text = message.sender.username,
                  fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                  style = TextStyle(color = Black),
                  modifier = Modifier.testTag("chat_message_sender_name"))
            }
            when (message) {
              is Message.TextMessage -> {
                Text(
                    text = message.text,
                    style = TextStyle(color = Black),
                    modifier = Modifier.testTag("chat_message_text"))
              }
              is Message.PhotoMessage -> {
                Image(
                    painter = rememberAsyncImagePainter(message.photoUri.toString()),
                    contentDescription = stringResource(R.string.contentDescription_photo),
                    modifier =
                        Modifier.size(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .testTag("chat_message_image"),
                    contentScale = ContentScale.Crop)
              }
              is Message.LinkMessage -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      painter = painterResource(id = R.drawable.link_24px),
                      contentDescription = stringResource(R.string.app_name),
                      tint = Blue)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                      text = message.linkName,
                      style = TextStyle(color = Blue),
                      modifier =
                          Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, message.linkUri)
                                browserLauncher.launch(intent)
                              }
                              .testTag("chat_message_link"))
                }
              }
              is Message.FileMessage -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      painter = painterResource(id = R.drawable.picture_as_pdf_24px),
                      contentDescription = stringResource(R.string.app_name),
                      tint = Blue)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                      text = message.fileName,
                      style = TextStyle(color = Blue),
                      modifier =
                          Modifier.clickable {
                                val intent =
                                    Intent().apply {
                                      action = Intent.ACTION_VIEW
                                      setDataAndType(message.fileUri, MessageVal.FILE_TYPE)
                                      flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                browserLauncher.launch(
                                    Intent.createChooser(
                                        intent,
                                        "Open with") // I tried to extract the string resource but
                                    // it didn't work
                                    )
                              }
                              .testTag("chat_message_file"))
                }
              }
              else -> {
                Text(
                    text = stringResource(R.string.unsupported_messageType),
                    style = TextStyle(color = Black),
                )
              }
            }
            Text(
                text = message.getTime(),
                style = TextStyle(color = Gray),
                modifier = Modifier.testTag("chat_message_time"))
          }
        }
  }
}

@Composable
fun MessageTextFields(
    onSend: (String) -> Unit,
    defaultText: String = "",
    showIconsOptions: MutableState<Boolean>
) {
  var textToSend by remember { mutableStateOf(defaultText) }
  OutlinedTextField(
      value = textToSend,
      onValueChange = { textToSend = it },
      modifier =
          Modifier.padding(8.dp)
              .fillMaxWidth()
              .background(White, RoundedCornerShape(20.dp))
              .testTag("chat_text_field"),
      shape = RoundedCornerShape(20.dp),
      textStyle = TextStyle(color = Black),
      keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
      keyboardActions =
          KeyboardActions(
              onSend = {
                if (textToSend.isNotBlank()) {
                  onSend(textToSend)
                  textToSend = ""
                }
              }),
      leadingIcon = {
        IconButton(
            modifier = Modifier.size(48.dp).padding(6.dp),
            onClick = {
              showIconsOptions.value = !showIconsOptions.value
              Log.d("MyPrint", "Icon clicked, showIconsOptions.value: ${showIconsOptions.value}")
            }) {
              Icon(
                  Icons.Outlined.Add,
                  contentDescription = stringResource(R.string.contentDescription_icon_add),
                  tint = Blue)
            }
      },
      trailingIcon = {
        IconButton(
            modifier = Modifier.size(48.dp).padding(6.dp).testTag("chat_send_button"),
            onClick = {
              if (textToSend.isNotBlank()) {
                onSend(textToSend)
                textToSend = ""
              }
            }) {
              Icon(
                  imageVector = Icons.AutoMirrored.Outlined.Send,
                  contentDescription = stringResource(R.string.contentDescription_icon_send),
                  tint = Blue)
            }
      },
      placeholder = { Text(stringResource(R.string.type_a_message)) })
}

@Composable
fun OptionsDialog(
    viewModel: MessageViewModel,
    selectedMessage: Message,
    showOptionsDialog: MutableState<Boolean>,
    showEditDialog: MutableState<Boolean>,
    navigationActions: NavigationActions
) {
  if (showOptionsDialog.value) {
    AlertDialog(
        onDismissRequest = { showOptionsDialog.value = false },
        title = { Text(text = stringResource(R.string.options)) },
        text = {
          Column(modifier = Modifier.testTag("option_dialog")) {
            Text(text = selectedMessage.getDate())
            if (viewModel.isUserMessageSender(selectedMessage)) {
              Spacer(modifier = Modifier.height(8.dp))
              if (selectedMessage is Message.TextMessage) {}

              when (selectedMessage) {
                // LinkMessage is commented due to the lack of update of the linkName field yet
                // (only the linkUri field is updated)
                is Message.TextMessage /*, is Message.LinkMessage*/ -> {
                  Button(
                      modifier = Modifier.testTag("option_dialog_edit"),
                      onClick = {
                        showEditDialog.value = true
                        showOptionsDialog.value = false
                      }) {
                        Text(
                            text = stringResource(R.string.edit),
                            style = TextStyle(color = White),
                        )
                      }
                  Spacer(modifier = Modifier.height(8.dp))
                }
                else -> {}
              }
              Button(
                  modifier = Modifier.testTag("option_dialog_delete"),
                  onClick = {
                    viewModel.deleteMessage(selectedMessage)
                    showOptionsDialog.value = false
                  }) {
                    Text(
                        text = stringResource(R.string.delete),
                        style = TextStyle(color = White),
                    )
                  }
            } else {
              if (viewModel.chat.type == ChatType.GROUP || viewModel.chat.type == ChatType.TOPIC) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    modifier = Modifier.testTag("option_dialog_start_direct_message"),
                    onClick = {
                      showOptionsDialog.value = false
                      viewModel.currentUser.value
                          ?.let { DirectMessageViewModel(it.uid) }
                          ?.startDirectMessage(selectedMessage.sender.uid)
                      navigationActions.navigateTo(Route.DIRECT_MESSAGE)
                    }) {
                      Text(
                          text = stringResource(R.string.start_direct_message),
                          style = TextStyle(color = White),
                      )
                    }
              }
            }
          }
        },
        confirmButton = {
          Button(
              modifier = Modifier.fillMaxWidth().testTag("option_dialog_cancel"),
              onClick = { showOptionsDialog.value = false }) {
                Text(text = stringResource(R.string.cancel), style = TextStyle(color = White))
              }
        })
  }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun EditDialog(
    viewModel: MessageViewModel,
    selectedMessage: Message,
    showEditDialog: MutableState<Boolean>
) {
  if (showEditDialog.value) {
    AlertDialog(
        onDismissRequest = { showEditDialog.value = false },
        title = { Text(text = stringResource(R.string.edit)) },
        text = {
          val selectedMessageText =
              when (selectedMessage) {
                is Message.TextMessage -> {
                  selectedMessage.text
                }
                is Message.LinkMessage -> {
                  selectedMessage.linkUri.toString()
                }
                else -> {
                  ""
                }
              }
          Column(modifier = Modifier.testTag("edit_dialog")) {
            MessageTextFields(
                onSend = {
                  viewModel.editMessage(selectedMessage, it)
                  showEditDialog.value = false
                },
                defaultText = selectedMessageText,
                mutableStateOf(false))
          }
        },
        confirmButton = {
          Button(
              modifier = Modifier.fillMaxWidth().testTag("edit_dialog_cancel"),
              onClick = { showEditDialog.value = false }) {
                Text(text = stringResource(R.string.cancel), style = TextStyle(color = White))
              }
        })
  }
}

@Composable
fun ChatGroupTitle(chat: Chat) {
  Image(
      painter = rememberAsyncImagePainter(chat.picture),
      contentDescription = stringResource(R.string.contentDescription_group_profile_picture),
      modifier = Modifier.size(40.dp).clip(CircleShape).testTag("group_title_profile_picture"),
      contentScale = ContentScale.Crop)

  Spacer(modifier = Modifier.width(8.dp))
  Column {
    Text(text = chat.name, maxLines = 1, modifier = Modifier.testTag("group_title_name"))
    Spacer(modifier = Modifier.width(8.dp))
    LazyRow(modifier = Modifier.testTag("group_title_members_row")) {
      items(chat.members) { member ->
        Text(
            text = member.username,
            modifier = Modifier.padding(end = 8.dp).testTag("group_title_member_name"),
            style = TextStyle(color = Gray),
            maxLines = 1)
      }
    }
  }
}

@Composable
fun PrivateChatTitle(chat: Chat) {
  Image(
      painter = rememberAsyncImagePainter(chat.picture),
      contentDescription = "User profile picture",
      modifier = Modifier.size(40.dp).clip(CircleShape).testTag("private_title_profile_picture"),
      contentScale = ContentScale.Crop)

  Spacer(modifier = Modifier.width(8.dp))
  Column { Text(text = chat.name, maxLines = 1, modifier = Modifier.testTag("private_title_name")) }
}

@Composable
fun IconsOptionsList(
    showIconsOptions: MutableState<Boolean>,
    showAddImage: MutableState<Boolean>,
    showAddLink: MutableState<Boolean>,
    showAddFile: MutableState<Boolean>
) {
  if (showIconsOptions.value) {
    AlertDialog(
        onDismissRequest = { showIconsOptions.value = false },
        text = {
          Column {
            LazyRow {
              items(3) {
                when (it) {
                  0 -> {
                    IconButton(
                        onClick = {
                          showIconsOptions.value = false
                          showAddImage.value = true
                        },
                        modifier = Modifier.padding(8.dp)) {
                          Icon(
                              painter = painterResource(id = R.drawable.image_24px),
                              contentDescription = stringResource(R.string.app_name),
                              tint = Blue)
                        }
                  }
                  1 -> {
                    IconButton(
                        onClick = {
                          showIconsOptions.value = false
                          showAddLink.value = true
                        },
                        modifier = Modifier.padding(8.dp)) {
                          Icon(
                              painter = painterResource(id = R.drawable.link_24px),
                              contentDescription = stringResource(R.string.app_name),
                              tint = Blue)
                        }
                  }
                  2 -> {
                    IconButton(
                        onClick = {
                          showIconsOptions.value = false
                          showAddFile.value = true
                        },
                        modifier = Modifier.padding(8.dp)) {
                          Icon(
                              painter = painterResource(id = R.drawable.picture_as_pdf_24px),
                              contentDescription = stringResource(R.string.app_name),
                              tint = Blue)
                        }
                  }
                }
              }
            }
          }
        },
        confirmButton = {
          Button(modifier = Modifier.fillMaxWidth(), onClick = { showIconsOptions.value = false }) {
            Text(text = stringResource(R.string.cancel), style = TextStyle(color = White))
          }
        })
  }
}

@Composable
fun SendPhotoMessage(messageViewModel: MessageViewModel, showAddImage: MutableState<Boolean>) {
  val photoState = remember { mutableStateOf(Uri.EMPTY) }
  val context = LocalContext.current

  val getContent =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { profilePictureUri -> photoState.value = profilePictureUri }
      }
  val imageInput = "image/*"

  val requestPermissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
          getContent.launch(imageInput)
        }
      }
  val permission = imagePermissionVersion()

  if (showAddImage.value) {
    AlertDialog(
        onDismissRequest = { showAddImage.value = false },
        text = {
          Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                SetPicture(photoState) {
                  checkPermission(context, permission, requestPermissionLauncher) {
                    getContent.launch(imageInput)
                  }
                }
              }
        },
        confirmButton = {
          SaveButton(photoState.value.toString().isNotBlank()) {
            messageViewModel.sendPhotoMessage(photoState.value)
            showAddImage.value = false
            photoState.value = Uri.EMPTY
          }
        })
  }
}

@Composable
fun SendLinkMessage(messageViewModel: MessageViewModel, showAddLink: MutableState<Boolean>) {
  val linkState = remember { mutableStateOf("") }
  val linkName = remember { mutableStateOf("") }

  if (showAddLink.value) {
    AlertDialog(
        onDismissRequest = { showAddLink.value = false },
        text = {
          Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                OutlinedTextField(
                    value = linkState.value,
                    onValueChange = { linkState.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Black),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.enter_link)) },
                )
              }
        },
        confirmButton = {
          SaveButton(linkState.value.isNotBlank()) {
            val uriString = linkState.value.trim()
            val uri =
                if (!isValidUrl(uriString)) {
                  Uri.parse("https://$uriString")
                } else {
                  Uri.parse(uriString)
                }
            linkName.value = uriString.substringAfter("//")

            messageViewModel.sendLinkMessage(linkName.value, uri)
            showAddLink.value = false
            linkState.value = ""
            linkName.value = ""
          }
        })
  }
}

fun isValidUrl(url: String): Boolean {
  return try {
    val uri = Uri.parse(url)
    uri.scheme == "http" || uri.scheme == "https"
  } catch (e: Exception) {
    false
  }
}

@Composable
fun SendFileMessage(messageViewModel: MessageViewModel, showAddFile: MutableState<Boolean>) {
  val fileState = remember { mutableStateOf(Uri.EMPTY) }
  val fileName = remember { mutableStateOf("") }
  val context = LocalContext.current

  val getContent =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { fileUri ->
          fileState.value = fileUri
          val cursor = context.contentResolver.query(fileUri, null, null, null, null)
          cursor?.use {
            if (it.moveToFirst()) {
              val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
              if (nameIndex != -1) {
                fileName.value = it.getString(nameIndex)
              }
            }
          }
        }
      }
  val fileInput = MessageVal.FILE_TYPE

  val requestPermissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
          getContent.launch(fileInput)
        }
      }
  val permission = imagePermissionVersion()

  if (showAddFile.value) {
    AlertDialog(
        onDismissRequest = { showAddFile.value = false },
        text = {
          Box(
              contentAlignment = Alignment.Center,
              modifier =
                  Modifier.padding(8.dp).fillMaxWidth().clickable {
                    checkPermission(context, permission, requestPermissionLauncher) {
                      getContent.launch(fileInput)
                    }
                  }) {
                if (fileState.value == Uri.EMPTY) {
                  Text(text = stringResource(R.string.select_a_file))
                } else {
                  Text(text = fileName.value)
                }
              }
        },
        confirmButton = {
          SaveButton(fileState.value.toString().isNotBlank()) {
            messageViewModel.sendFileMessage(fileName.value, fileState.value)
            showAddFile.value = false
            fileState.value = Uri.EMPTY
            fileName.value = ""
          }
        })
  }
}
