package com.github.se.studybuddies.tests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studybuddies.database.MockDatabase
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.ui.groups.GroupsSettingsButton
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LeaveGroupTest : TestCase() {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK lateinit var mockNavActions: NavigationActions

  private val db = MockDatabase()

  @Before
  fun setUp() {
    composeTestRule.setContent { GroupsSettingsButton("userTest", mockNavActions, db) }
  }

  @Test
  fun testLeaveGroupDisplay() {
    /*ComposeScreen.onComposeScreen<GroupsHomeScreen>(
    composeTestRule) {
    GroupsSettingsButton { assertIsDisplayed() }
      textDialogues { assertIsDisplayed() }
      textDialoguesYes { assertIsDisplayed() }
      textDialoguesNo { assertIsDisplayed() }
    }*/
  }
}
