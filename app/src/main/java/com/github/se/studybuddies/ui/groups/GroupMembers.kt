package com.github.se.studybuddies.ui.groups

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberImagePainter
import com.github.se.studybuddies.R
import com.github.se.studybuddies.data.Group
import com.github.se.studybuddies.data.User
import com.github.se.studybuddies.database.DbRepository
import com.github.se.studybuddies.navigation.GROUPS_MEMBERS_DESTINATIONS
import com.github.se.studybuddies.navigation.GROUPS_SETTINGS_DESTINATIONS
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.navigation.Route
import com.github.se.studybuddies.permissions.checkPermission
import com.github.se.studybuddies.permissions.imagePermissionVersion
import com.github.se.studybuddies.ui.shared_elements.GoBackRouteButton
import com.github.se.studybuddies.ui.shared_elements.MainScreenScaffold
import com.github.se.studybuddies.ui.shared_elements.SaveButton
import com.github.se.studybuddies.ui.shared_elements.SearchIcon
import com.github.se.studybuddies.ui.shared_elements.Sub_title
import com.github.se.studybuddies.ui.shared_elements.TopNavigationBar
import com.github.se.studybuddies.ui.theme.Blue
import com.github.se.studybuddies.ui.theme.White
import com.github.se.studybuddies.viewModels.GroupViewModel
import com.github.se.studybuddies.viewModels.GroupsHomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MutableCollectionMutableState")
@Composable
fun GroupMembers(
    groupUID: String,
    groupViewModel: GroupViewModel,
    navigationActions: NavigationActions,
    db: DbRepository
) {

    if (groupUID.isEmpty()) return
    groupViewModel.fetchGroupData(groupUID)
    val groupData by groupViewModel.group.observeAsState()

    val nameState = remember { mutableStateOf(groupData?.name ?: "") }
    val members = remember { groupData?.let { mutableStateOf (it.members) } }

    groupData?.let {
        nameState.value = it.name
        if (members != null) {
            members.value = it.members
        }
    }

    val userDatas = remember { mutableStateOf(mutableListOf<User>()) }

    if (members != null) {
        for (member in members.value) {
            groupViewModel.fetchUserData(member)
            val userData by groupViewModel.member.observeAsState()
            userData?.let { userDatas.value.add(it) }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .testTag("members_scaffold"),
        topBar = {
            TopNavigationBar(
                title = { Sub_title("Members") },
                navigationIcon = {
                    GoBackRouteButton(navigationActions = navigationActions, Route.GROUPSHOME)
                },
                actions = { GroupsSettingsButton(groupUID, navigationActions, db) })
        }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top) {
            LazyColumn(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .testTag("draw_member_column"),
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally) {
                item { Spacer(modifier = Modifier.padding(10.dp)) }
                item { Name(nameState) }
                item { Spacer(modifier = Modifier.padding(10.dp)) }
                if (members != null) {
                    items(userDatas.value) { member -> MemberItem(groupUID, member, navigationActions, db) }
                } else { // Should never happen
                    item {
                        Text(
                            "Error, no member found for this group",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("EmptyGroupMemberText"))
                    }
                }
                //item { add member button } will be added later
                }
            }
        }
    }

@Composable
fun Name(nameState: MutableState<String>) {
    Spacer(Modifier.height(20.dp))
    Text(
        nameState.value,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("ShowGroupNameInGroupMember"))
}

@Composable
fun MemberItem(groupUID: String, user: User, navigationActions: NavigationActions, db: DbRepository) {
    Box(
        modifier =
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .drawBehind {
                val strokeWidth = 1f
                val y = size.height - strokeWidth / 2
                drawLine(Color.LightGray, Offset(0f, y), Offset(size.width, y), strokeWidth)
            }
            .testTag(user.username + "_box")) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Box(modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.Transparent)) {
                Image(
                    painter = rememberImagePainter(user.photoUrl),
                    contentDescription = stringResource(id = R.string.user_picture),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = user.username,
                modifier = Modifier.align(Alignment.CenterVertically),
                style = TextStyle(fontSize = 20.sp),
                lineHeight = 28.sp)
            Spacer(modifier = Modifier.weight(1f))
            MemberOptionButton(groupUID, user.uid, user.username, navigationActions, db)
        }
    }
}

@Composable
fun MemberOptionButton(groupUID: String, userUID: String, username: String, navigationActions: NavigationActions, db: DbRepository) {
    var isRemoveUserDialogVisible by remember { mutableStateOf(false) }
    val expandedState = remember { mutableStateOf(false) }
    val groupViewModel = GroupViewModel(groupUID, db)
    Row {
        IconButton(
            onClick = { expandedState.value = true },
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                tint = Blue,
                contentDescription = stringResource(R.string.dots_menu))
        }
        DropdownMenu(
            expanded = expandedState.value,
            onDismissRequest = { expandedState.value = false }) {
            GROUPS_MEMBERS_DESTINATIONS.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        expandedState.value = false
                        when (item.route) {
                            Route.USERREMOVE -> {
                                isRemoveUserDialogVisible = true
                            }
                        }
                    }) {
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(item.textId)
                }
            }
        }
    }
    if (isRemoveUserDialogVisible) {
        Dialog(onDismissRequest = { isRemoveUserDialogVisible = false }) {
            Box(
                modifier =
                Modifier
                    .width(280.dp)
                    .height(140.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Are you sure you want to remove $username from the group ?",
                        color = Blue)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(
                            onClick = {
                                groupViewModel.leaveGroup(groupUID, userUID)
                                navigationActions.navigateTo("${Route.GROUPMEMBERS}/$groupUID")
                                isRemoveUserDialogVisible = false
                            },
                            modifier =
                            Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .width(80.dp)
                                .height(40.dp),
                            colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color.Red, contentColor = White)) {
                            Text(text = stringResource(R.string.yes))
                        }

                        Button(
                            onClick = { isRemoveUserDialogVisible = false },
                            modifier =
                            Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .width(80.dp)
                                .height(40.dp),
                            colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Blue, contentColor = White)) {
                            Text(text = stringResource(R.string.no))
                        }
                    }
                }
            }
        }
    }
}