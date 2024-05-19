package com.github.se.studybuddies.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studybuddies.data.Group
import com.github.se.studybuddies.data.TopicList
import com.github.se.studybuddies.data.User
import com.github.se.studybuddies.database.DatabaseConnection
import com.github.se.studybuddies.database.StorageDatabaseConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupViewModel(uid: String? = null) : ViewModel() {
  private val db = DatabaseConnection()
  private val _group = MutableLiveData(Group.empty())
  private val _members = MutableLiveData<List<User>>(emptyList())
  val members: LiveData<List<User>> = _members
  val group: LiveData<Group> = _group
  private val _topics = MutableStateFlow(TopicList(emptyList()))
  val topics = _topics.asStateFlow()

  init {
    if (uid != null) {
      fetchGroupData(uid)
      fetchUsers()
      subscribeToTopics(uid)
    }
  }

  fun fetchGroupData(uid: String) {
    viewModelScope.launch { _group.value = db.getGroup(uid) }
  }

  private fun subscribeToTopics(uid: String) {
    db.getAllTopics(uid, viewModelScope, Dispatchers.IO, Dispatchers.Main) { topicList ->
      _topics.value = topicList
    }
  }

  fun createGroup(name: String, photoUri: Uri) {
    viewModelScope.launch { db.createGroup(name, photoUri) }
  }

  suspend fun getDefaultPicture(): Uri {
    return withContext(Dispatchers.IO) { StorageDatabaseConnection().getDefaultGroupPicture() }
  }

  fun fetchUsers() {
    viewModelScope.launch {
      _group.value?.members?.let { memberIds ->
        val users = memberIds.map { uid -> db.getUser(uid) }
        _members.postValue(users)
      }
    }
  }

  fun leaveGroup(groupUID: String, userUID: String = "") {
    viewModelScope.launch { db.removeUserFromGroup(groupUID, userUID) }
  }

  fun deleteGroup(groupUID: String) {
    viewModelScope.launch { db.deleteGroup(groupUID) }
  }

  fun addUserToGroup(groupUID: String, text: String = "") {
    viewModelScope.launch { db.addUserToGroup(groupUID, text) }
  }

  fun updateGroup(groupUID: String, name: String, photoURI: Uri?) {
    if (photoURI != null) {
      db.updateGroup(groupUID, name, photoURI)
    }
  }

  fun createGroupInviteLink(groupUID: String, groupName: String): String {
    if (groupUID == "") {
      Log.d("MyPrint", "The Group id is empty")
      return "Current group not found"
    } else if (groupName == "") {
      Log.d("MyPrint", "The Group name is empty")
      return "Current group not found"
    } else {
      val link = "studybuddiesJoinGroup=$groupName/$groupUID"
      Log.d("MyPrint", "Successfully created the link")
      return link
    }
  }

  // function not used anymore, but can be useful for future development if wanting to add a Dynamic
  // Link
  /*fun checkIncomingDynamicLink(
      intent: Intent,
      activity: Activity,
      navigationActions: NavigationActions
  ) {
    FirebaseDynamicLinks.getInstance()
        .getDynamicLink(intent)
        .addOnSuccessListener(activity) { pendingDynamicLinkData: PendingDynamicLinkData? ->
            // Get deep link from result (may be null if no link is found)
            if (pendingDynamicLinkData != null) {
                val deepLink = pendingDynamicLinkData.link

            Log.d("Link", "Dynamic Link Detected")
          // Handle the deep link.
          val groupUID = deepLink.toString().substringAfterLast("/")
            Log.d("Link", "Group to join : $groupUID")
          if (groupUID != "") {
            val currentUserUid =
                FirebaseAuth.getInstance().currentUser?.uid // Get the current user's UID

            if (currentUserUid != null) {
              // Add the current user to the group in your Firebase database
              db.updateGroup(groupUID)
                Log.d("Link", "Go add user to group")
              // Go to the newly joined group
              navigationActions.navigateTo("${Route.GROUP}/$groupUID")
            } else {
              // If the user is not logged go to login page (link will have to be clicked again)
                Log.d("Link", "The user is not logged in")
              navigationActions.navigateTo(Route.LOGIN)
            }
          }
            }
        }
        .addOnFailureListener(activity) { e -> Log.w("Link", "getDynamicLink:onFailure", e) }
  }*/

}
