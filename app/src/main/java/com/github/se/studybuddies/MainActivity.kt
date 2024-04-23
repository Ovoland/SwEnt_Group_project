package com.github.se.studybuddies

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.navigation.Route
import com.github.se.studybuddies.ui.ChatScreen
import com.github.se.studybuddies.ui.LoginScreen
import com.github.se.studybuddies.ui.groups.CreateGroup
import com.github.se.studybuddies.ui.groups.GroupScreen
import com.github.se.studybuddies.ui.groups.GroupsHome
import com.github.se.studybuddies.ui.settings.AccountSettings
import com.github.se.studybuddies.ui.settings.CreateAccount
import com.github.se.studybuddies.ui.settings.Settings
import com.github.se.studybuddies.ui.solo_study.SoloStudyHome
import com.github.se.studybuddies.ui.theme.StudyBuddiesTheme
import com.github.se.studybuddies.viewModels.GroupViewModel
import com.github.se.studybuddies.viewModels.GroupsHomeViewModel
import com.github.se.studybuddies.viewModels.MessageViewModel
import com.github.se.studybuddies.viewModels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    auth = FirebaseAuth.getInstance()

    setContent {
      StudyBuddiesTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          val navController = rememberNavController()
          val navigationActions = NavigationActions(navController)
          val currentUser = auth.currentUser
          val startDestination =
              if (currentUser != null) {
                Route.SOLOSTUDYHOME
              } else {
                Route.LOGIN
              }
          NavHost(navController = navController, startDestination = startDestination) {
            composable(Route.LOGIN) {
              LoginScreen(navigationActions)
              Log.d("MyPrint", "Successfully navigated to LoginScreen")
            }
            composable(Route.GROUPSHOME) {
              if (currentUser != null) {
                GroupsHome(currentUser.uid, GroupsHomeViewModel(currentUser.uid), navigationActions)
                Log.d("MyPrint", "Successfully navigated to GroupsHome")
              }
            }
            composable(
                route = "${Route.GROUP}/{groupUID}",
                arguments = listOf(navArgument("groupUID") { type = NavType.StringType })) {
                    backStackEntry ->
                  val groupUID = backStackEntry.arguments?.getString("groupUID")
                  if (groupUID != null) {
                    GroupScreen(groupUID, GroupViewModel(groupUID), navigationActions)
                    Log.d("MyPrint", "Successfully navigated to GroupScreen")
                  }
                }
            composable(
                route = "${Route.SETTINGS}/{backRoute}",
                arguments = listOf(navArgument("backRoute") { type = NavType.StringType })) {
                    backStackEntry ->
                  val backRoute = backStackEntry.arguments?.getString("backRoute")
                  if (backRoute != null) {
                    Settings(backRoute, navigationActions)
                    Log.d("MyPrint", "Successfully navigated to Settings")
                  }
                }
            composable(
                route = "${Route.ACCOUNT}/{backRoute}",
                arguments = listOf(navArgument("backRoute") { type = NavType.StringType })) {
                    backStackEntry ->
                  val backRoute = backStackEntry.arguments?.getString("backRoute")
                  if (backRoute != null && currentUser != null) {
                    AccountSettings(
                        currentUser.uid,
                        UserViewModel(currentUser.uid),
                        backRoute,
                        navigationActions)
                    Log.d("MyPrint", "Successfully navigated to Settings")
                  }
                }
            composable(Route.CREATEACCOUNT) {
              if (currentUser != null) {
                CreateAccount(UserViewModel(), navigationActions)
                Log.d("MyPrint", "Successfully navigated to CreateAccount")
              }
            }
            composable(Route.CREATEGROUP) {
              if (currentUser != null) {
                CreateGroup(GroupViewModel(), navigationActions)
                Log.d("MyPrint", "Successfully navigated to CreateGroup")
              }
            }
            composable(Route.CHAT) {
              if (currentUser != null) {
                ChatScreen(MessageViewModel("general_group"), navigationActions)
              }
            }
            composable(Route.SOLOSTUDYHOME) {
              if (currentUser != null) {
                SoloStudyHome(navigationActions)
                Log.d("MyPrint", "Successfully navigated to SoloStudyHome")
              }
            }
          }
            // For the group invitation link
            FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    var deepLink: Uri? = null
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.link
                    }

                    // Handle the deep link.
                    val groupUID = deepLink?.lastPathSegment?.toString()
                    if (groupUID != null) {
                        val currentUserUid =
                            FirebaseAuth.getInstance().currentUser?.uid // Get the current user's UID
                        if (currentUserUid != null) {
                            // Add the current user to the group in your Firebase database
                            val db = Firebase.firestore
                            val userUID = FirebaseAuth.getInstance().currentUser?.uid

                            val userMembershipRef = db.collection("userMembership").document(userUID!!)
                            userMembershipRef.update("groups", FieldValue.arrayUnion(groupUID))
                                //.addOnSuccessListener { Log.d(TAG, "User updated") }
                                //.addOnFailureListener { e -> Log.w(TAG, "Error updating user", e) }

                            val groupDataRef = db.collection("groupData").document(groupUID)
                            groupDataRef.update("members", FieldValue.arrayUnion(userUID))
                                //.addOnSuccessListener { Log.d(TAG, "Group updated") }
                                //.addOnFailureListener { e -> Log.w(TAG, "Error updating group", e) }
                        }
                        //Go to the newly joined group
                        navigationActions.navigateTo("${Route.GROUP}/$groupUID")
                    }
                }
                .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
        }
      }
    }
  }
}
