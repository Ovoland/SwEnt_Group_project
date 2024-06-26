package com.github.se.studybuddies.tests

import android.content.Context
import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studybuddies.data.User
import com.github.se.studybuddies.database.DatabaseConnection
import com.github.se.studybuddies.database.MockDatabase
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.screens.MapScreen
import com.github.se.studybuddies.ui.map.MapScreen
import com.github.se.studybuddies.viewModels.ContactsViewModel
import com.github.se.studybuddies.viewModels.UserViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  // Relaxed mocks methods have a default implementation returning values
  @RelaxedMockK lateinit var mockNavActions: NavigationActions

  // Use the userTest created manually in the database
  private val uid = "userTest"
  private val userTest =
      User(
          uid = uid,
          email = "test@gmail.com",
          username = "testUser",
          photoUrl =
              Uri.parse("https://images.pexels.com/photos/6031345/pexels-photo-6031345.jpeg"),
          location = "offline")
  private val db = DatabaseConnection()
  private val userVM = UserViewModel(uid, db)
  private val contactsViewModel = ContactsViewModel(uid, db)

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    composeTestRule.setContent {
      MapScreen(
          uid = uid,
          userVM,
          contactsViewModel,
          navigationActions = mockNavActions,
          context = context)
    }
  }

  @Test
  fun elementsAreDisplayed() {
    onComposeScreen<MapScreen>(composeTestRule) {
      // The Fakedata are now loading so fast that we don't have the time to test the loading circle
      loading { assertIsDisplayed() }
      mapIcon { assertIsDisplayed() }
      mapScreen { assertIsDisplayed() }
    }
  }
}

@RunWith(AndroidJUnit4::class)
class MapDatabase : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  // Relaxed mocks methods have a default implementation returning values
  @RelaxedMockK lateinit var mockNavActions: NavigationActions

  private val uid = "userTest1"
  private val db = MockDatabase()
  private val userVM = UserViewModel(uid, db)
  private val contactsViewModel = ContactsViewModel(uid, db)

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    composeTestRule.setContent {
      MapScreen(
          uid = uid,
          userVM,
          contactsViewModel,
          navigationActions = mockNavActions,
          context = context)
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun getUserFriends() {
    onComposeScreen<MapScreen>(composeTestRule) {
      contactsViewModel.fetchAllFriends(uid)
      val friends = contactsViewModel.friends.value
      // After the delay, the friends list should be finally retrieved
      assert(friends.getAllTasks().isNotEmpty())
    }
  }

  /*TODO() google certificates issue
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun getAllUser() {
    onComposeScreen<MapScreen>(composeTestRule) {
      contactsViewModel.fetchAllUsers()
      val users = contactsViewModel.allUsers.value
      assert(users.isNotEmpty())
    }
  }

    */

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun updateUserLocation() {
    onComposeScreen<MapScreen>(composeTestRule) {
      userVM.fetchUserData(uid)
      // Set the location of the user to online
      userVM.updateLocation(uid, "20.0,30.0")
      // Now the location of the user should be online
      val location = userVM.userData.value?.location
      assert(location == "offline")
    }
  }
}
