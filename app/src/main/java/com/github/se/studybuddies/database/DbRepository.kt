package com.github.se.studybuddies.database

import android.net.Uri
import com.github.se.studybuddies.data.Chat
import com.github.se.studybuddies.data.ChatType
import com.github.se.studybuddies.data.Contact
import com.github.se.studybuddies.data.ContactList
import com.github.se.studybuddies.data.DailyPlanner
import com.github.se.studybuddies.data.Group
import com.github.se.studybuddies.data.GroupList
import com.github.se.studybuddies.data.Message
import com.github.se.studybuddies.data.Topic
import com.github.se.studybuddies.data.TopicFile
import com.github.se.studybuddies.data.TopicFolder
import com.github.se.studybuddies.data.TopicItem
import com.github.se.studybuddies.data.TopicList
import com.github.se.studybuddies.data.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

interface DbRepository {
  fun isFakeDatabase(): Boolean

  // using the userData collection
  suspend fun getUser(uid: String): User?

  suspend fun getCurrentUser(): User

  suspend fun getContact(contactID: String): Contact


  suspend fun getAllContacts(uid: String): ContactList

  fun getCurrentUserUID(): String

  suspend fun getAllFriends(uid: String): List<User>

  suspend fun getDefaultProfilePicture(): Uri

  suspend fun createUser(
      uid: String,
      email: String,
      username: String,
      profilePictureUri: Uri,
      location: String = "offline"
  )

  fun updateUserData(
      uid: String,
      email: String,
      username: String,
      profilePictureUri: Uri,
      location: String
  )

  fun updateLocation(uid: String, location: String)

  fun userExists(uid: String, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit)

  fun groupExists(groupUID: String, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit)

  // using the groups & userMemberships collections
  suspend fun getAllGroups(uid: String): GroupList

  suspend fun updateGroupTimer(groupUID: String, newEndTime: Long, newIsRunning: Boolean): Int

  suspend fun getGroup(groupUID: String): Group

  suspend fun getGroupName(groupUID: String): String

  suspend fun getDefaultPicture(): Uri

  suspend fun createGroup(name: String, photoUri: Uri)

  suspend fun addUserToGroup(groupUID: String, user: String = "")

  fun updateGroup(groupUID: String, name: String, photoUri: Uri)

  suspend fun removeUserFromGroup(groupUID: String, userUID: String = "")

  suspend fun deleteGroup(groupUID: String)

  fun sendMessage(chatUID: String, message: Message, chatType: ChatType, additionalUID: String = "")

  fun saveMessage(path: String, data: Map<String, Any>)

  fun uploadChatImage(uid: String, chatUID: String, imageUri: Uri, callback: (Uri?) -> Unit)

  fun deleteMessage(groupUID: String, message: Message, chatType: ChatType)

  suspend fun removeTopic(uid: String)

  fun editMessage(groupUID: String, message: Message, chatType: ChatType, newText: String)

  fun getMessagePath(chatUID: String, chatType: ChatType, additionalUID: String = ""): String

  fun getGroupMessagesPath(groupUID: String): String

  fun getTopicMessagesPath(groupUID: String, topicUID: String): String

  fun getPrivateMessagesPath(chatUID: String): String

  fun getPrivateChatMembersPath(chatUID: String): String

  fun subscribeToPrivateChats(
      userUID: String,
      scope: CoroutineScope,
      ioDispatcher: CoroutineDispatcher,
      mainDispatcher: CoroutineDispatcher,
      onUpdate: (List<Chat>) -> Unit
  )

  fun getMessages(
      chat: Chat,
      liveData: MutableStateFlow<List<Message>>,
      ioDispatcher: CoroutineDispatcher,
      mainDispatcher: CoroutineDispatcher
  )

  fun checkForExistingChat(
      currentUserUID: String,
      otherUID: String,
      onResult: (Boolean, String?) -> Unit
  )

  suspend fun startDirectMessage(otherUID: String): String

  // using the topicData and topicItemData collections
  suspend fun getTopic(uid: String): Topic

  suspend fun fetchTopicItems(listUID: List<String>): List<TopicItem>

  fun createTopic(name: String, callBack: (String) -> Unit)

  suspend fun addTopicToGroup(topicUID: String, groupUID: String)

  fun addExercise(uid: String, exercise: TopicItem)

  fun addTheory(uid: String, theory: TopicItem)

  suspend fun deleteTopic(topicId: String, groupUID: String, callBack: () -> Unit)

  fun updateTopicName(uid: String, name: String)

  fun createTopicFolder(name: String, parentUID: String, callBack: (TopicFolder) -> Unit)

  fun createTopicFile(name: String, parentUID: String, callBack: (TopicFile) -> Unit)

  fun updateTopicItem(item: TopicItem)

  fun getTimerUpdates(groupUID: String, _timerValue: MutableStateFlow<Long>): Boolean

  fun updateDailyPlanners(uid: String, dailyPlanners: List<DailyPlanner>)

  fun getAllTopics(
      groupUID: String,
      scope: CoroutineScope,
      ioDispatcher: CoroutineDispatcher,
      mainDispatcher: CoroutineDispatcher,
      onUpdate: (TopicList) -> Unit
  )

  suspend fun createContact(otherUID: String, contactID: String)


  fun deleteContact(contactID: String)

  companion object {
    const val topic_name = "name"
    const val topic_exercises = "exercises"
    const val topic_theory = "theory"
    const val item_parent = "parent"
    const val item_type = "type"
    const val item_items = "items"
    const val item_strongUsers = "strongUsers"
  }
}
