package com.github.se.studybuddies.database

import android.net.Uri
import android.util.Log
import com.github.se.studybuddies.data.Group
import com.github.se.studybuddies.data.GroupList
import com.github.se.studybuddies.data.Location
import com.github.se.studybuddies.data.Message
import com.github.se.studybuddies.data.MessageVal
import com.github.se.studybuddies.data.User
import com.github.se.studybuddies.data.todo.ToDo
import com.github.se.studybuddies.data.todo.ToDoList
import com.github.se.studybuddies.data.todo.ToDoStatus
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.tasks.await

class DatabaseConnection {
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private val storage = FirebaseStorage.getInstance().reference
  private val todoCollection = db.collection("toDoList")

  val rt_db =
      Firebase.database(
          "https://study-buddies-e655a-default-rtdb.europe-west1.firebasedatabase.app/")

  // all collections
  private val userDataCollection = db.collection("userData")
  private val userMembershipsCollection = db.collection("userMemberships")
  private val groupDataCollection = db.collection("groupData")

  // using the userData collection
  fun getCurrentUserUID(): String {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    return if (uid != null) {
      Log.d("MyPrint", "Fetched user UID is $uid")
      uid
    } else {
      Log.d("MyPrint", "Failed to get current user UID")
      ""
    }
  }

  fun getUserData(uid: String): Task<DocumentSnapshot> {
    return userDataCollection.document(uid).get()
  }

  suspend fun getDefaultProfilePicture(): Uri {
    return storage.child("userData/default.jpg").downloadUrl.await()
  }

  fun createUser(uid: String, email: String, username: String, profilePictureUri: Uri) {
    val user =
        hashMapOf(
            "email" to email, "username" to username, "photoUrl" to profilePictureUri.toString())
    userDataCollection
        .document(uid)
        .set(user)
        .addOnSuccessListener {
          val pictureRef = storage.child("userData/$uid/profilePicture.jpg")
          pictureRef
              .putFile(profilePictureUri)
              .addOnSuccessListener {
                pictureRef.downloadUrl.addOnSuccessListener { uri ->
                  userDataCollection.document(uid).update("photoUrl", uri.toString())
                }
                Log.e("MyPrint", "User data successfully created")
              }
              .addOnFailureListener { e ->
                Log.d(
                    "MyPrint",
                    "Failed to upload photo with error with link $profilePictureUri: ",
                    e)
              }
          Log.d("MyPrint", "User data successfully created for uid $uid")
        }
        .addOnFailureListener { e ->
          Log.d("MyPrint", "Failed to create user data with error: ", e)
        }

    val membership = hashMapOf("groups" to emptyList<String>())
    userMembershipsCollection
        .document(uid)
        .set(membership)
        .addOnSuccessListener { Log.d("MyPrint", "User memberships successfully created") }
        .addOnFailureListener { e ->
          Log.d("MyPrint", "Failed to create user memberships with error: ", e)
        }
  }

  fun updateUserData(uid: String, email: String, username: String, profilePictureUri: Uri) {
    val task = hashMapOf("email" to email, "username" to username)
    userDataCollection
        .document(uid)
        .update(task as Map<String, Any>)
        .addOnSuccessListener {
          val pictureRef = storage.child("userData/$uid/profilePicture.jpg")
          pictureRef
              .putFile(profilePictureUri)
              .addOnSuccessListener {
                pictureRef.downloadUrl.addOnSuccessListener { uri ->
                  userDataCollection.document(uid).update("photoUrl", uri.toString())
                }
              }
              .addOnFailureListener { e ->
                Log.d("MyPrint", "Failed to upload photo with error: ", e)
              }
          Log.d("MyPrint", "User data successfully updated")
        }
        .addOnFailureListener { e ->
          Log.d("MyPrint", "Failed to update user data with error: ", e)
        }
  }

  fun userExists(uid: String, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit) {
    userDataCollection
        .document(uid)
        .get()
        .addOnSuccessListener { document -> onSuccess(document.exists()) }
        .addOnFailureListener { e -> onFailure(e) }
  }

  // using the groups & userMemberships collections
  suspend fun getAllGroups(uid: String): GroupList {
    try {
      val snapshot = userMembershipsCollection.document(uid).get().await()
      val items = mutableListOf<Group>()

      if (snapshot.exists()) {
        val groupUIDs = snapshot.data?.get("groups") as? List<String>
        groupUIDs?.let { groupsIDs ->
          groupsIDs.forEach { groupUID ->
            val document = groupDataCollection.document(groupUID.toString()).get().await()
            val name = document.getString("name") ?: ""
            val photo = Uri.parse(document.getString("picture") ?: "")
            val members = document.get("members") as? List<String> ?: emptyList()
            items.add(Group(groupUID, name, photo, members))
          }
        }
        return GroupList(items)
      } else {
        Log.d("MyPrint", "User with uid $uid does not exist")
        return GroupList(emptyList())
      }
    } catch (e: Exception) {
      Log.d("MyPrint", "In ViewModel, could not fetch groups with error: $e")
    }
    return GroupList(emptyList())
  }

  fun getGroupData(groupUID: String): Task<DocumentSnapshot> {
    return groupDataCollection.document(groupUID).get()
  }

  suspend fun getDefaultPicture(): Uri {
    return storage.child("groupData/default_group.jpg").downloadUrl.await()
  }

  fun createGroup(name: String, photoUri: Uri) {
    val uid = getCurrentUserUID()
    val group =
        hashMapOf("name" to name, "picture" to photoUri.toString(), "members" to listOf(uid))
    groupDataCollection
        .add(group)
        .addOnSuccessListener { documentReference ->
          val groupUID = documentReference.id
          userMembershipsCollection
              .document(uid)
              .update("groups", FieldValue.arrayUnion(groupUID))
              .addOnSuccessListener { Log.d("MyPrint", "Group successfully created") }
              .addOnFailureListener { e ->
                Log.d("MyPrint", "Failed to update user memberships with error: ", e)
              }
          val pictureRef = storage.child("groupData/$groupUID/picture.jpg")
          pictureRef
              .putFile(photoUri)
              .addOnSuccessListener {
                pictureRef.downloadUrl.addOnSuccessListener { uri ->
                  groupDataCollection.document(groupUID).update("picture", uri.toString())
                }
              }
              .addOnFailureListener { e ->
                Log.d("MyPrint", "Failed to upload photo with error: ", e)
              }
        }
        .addOnFailureListener { e -> Log.d("MyPrint", "Failed to create group with error: ", e) }
  }

  fun sendGroupMessage(groupUID: String, message: Message) {
    val messagePath = getGroupMessagesPath(groupUID) + "/${message.uid}"
    val messageData =
        mapOf(
            MessageVal.TEXT to message.text,
            MessageVal.SENDER_UID to message.sender.uid,
            MessageVal.TIMESTAMP to message.timestamp)
    rt_db
        .getReference(messagePath)
        .updateChildren(messageData)
        .addOnSuccessListener { Log.d("MessageSend", "Message successfully written!") }
        .addOnFailureListener { Log.w("MessageSend", "Failed to write message.", it) }
  }

  fun getUser(uid: String): User {
    // TODO implement this method (or modify getUserData to return User object)
    return User(uid, "email", "username - ${uid.take(5)}", Uri.parse("photoUrl"))
  }

  fun getCurrentUser(): User {
    return getUser(getCurrentUserUID())
  }

  fun getGroupMessagesPath(groupUID: String): String {
    return MessageVal.GROUPS + "/$groupUID/" + MessageVal.MESSAGES
  }

  fun updateTodo(
      todoId: String,
      name: String,
      assigneeName: String,
      dueDate: Date,
      location: String,
      description: String,
      status: String
  ) {
    val task =
        hashMapOf(
            "title" to name,
            "assigneeName" to assigneeName,
            "dueDate" to dueDate,
            "location" to location,
            "description" to description,
            "status" to status)
    todoCollection
        .document(todoId)
        .update(task as Map<String, Any>)
        .addOnSuccessListener { Log.d("MyPrint", "Task $todoId succesfully updated") }
        .addOnFailureListener { Log.d("MyPrint", "Task $todoId failed to update") }
  }

  suspend fun getAllItems(): ToDoList {
    val querySnapshot = todoCollection.get().await()
    val items = mutableListOf<ToDo>()

    for (document in querySnapshot.documents) {
      val uid = document.id
      val name = document.getString("title") ?: ""
      val assigneeName = document.getString("assigneeName") ?: ""
      val dueDate = document.getDate("dueDate")
      val convertedDate = dueDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
      val locationString = document.getString("location") ?: ""
      val location = Location.fromString(locationString)
      val description = document.getString("description") ?: ""
      val status = ToDoStatus.valueOf(document.getString("status") ?: "")

      val item = ToDo(uid, name, assigneeName, convertedDate, location, description, status)
      items.add(item)
    }

    return ToDoList(items)
  }

  fun addNewTodo(
      name: String,
      assigneeName: String,
      dueDate: Date,
      location: String,
      description: String,
      status: String
  ) {
    val task =
        hashMapOf(
            "title" to name,
            "assigneeName" to assigneeName,
            "dueDate" to dueDate,
            "location" to location,
            "description" to description,
            "status" to status)
    todoCollection
        .add(task)
        .addOnSuccessListener { Log.d("MyPrint", "Task succesfully added") }
        .addOnFailureListener { Log.d("MyPrint", "Failed to add task") }
  }

  fun fetchTaskByUID(uid: String): Task<DocumentSnapshot> {
    return todoCollection.document(uid).get()
  }

  fun deleteTodo(todoId: String) {
    todoCollection
        .document(todoId)
        .delete()
        .addOnSuccessListener { Log.d("MyPrint", "Successfully deleted task") }
        .addOnFailureListener { Log.d("MyPrint", "Failed to delete task") }
  }
}
