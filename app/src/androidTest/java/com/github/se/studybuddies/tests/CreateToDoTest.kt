package com.github.se.studybuddies.tests

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.screens.CreateToDoScreen
import com.github.se.studybuddies.ui.todo.CreateToDo
import com.github.se.studybuddies.viewModels.ToDoListViewModel
import com.google.common.base.Verify.verify
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateToDoTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createComposeRule()

  // This rule automatic initializes lateinit properties with @MockK, @RelaxedMockK, etc.
  @get:Rule val mockkRule = MockKRule(this)

  // Relaxed mocks methods have a default implementation returning values
  @RelaxedMockK lateinit var mockNavActions: NavigationActions

  private lateinit var toDoListViewModel: ToDoListViewModel

  @Before
  fun testSetup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    toDoListViewModel = ToDoListViewModel(context)
    composeTestRule.setContent { CreateToDo(toDoListViewModel, mockNavActions) }
  }

  @Test
  fun topAppBarTest() = run {
    onComposeScreen<CreateToDoScreen>(composeTestRule) {
      topAppBox {
        // arrange: verify pre-conditions
        assertIsDisplayed()
      }

      topAppBar {
        // arrange: verify pre-conditions
        assertIsDisplayed()
      }
      divider {
        // arrange: verify pre-conditions
        assertIsDisplayed()
      }
      goBackButton {
        // arrange: verify pre-conditions
        assertIsDisplayed()
        performClick()
      }
    }
    // assert: the nav action has been called
    verify { mockNavActions.goBack() }
    confirmVerified(mockNavActions)
  }

  @Test
  fun inputTaskName() {
    onComposeScreen<CreateToDoScreen>(composeTestRule) {
      saveButton { assertIsNotEnabled() }
      todoNameField {
        performTextClearance()
        performTextInput("Official Task Testing")
        assertTextContains("Official Task Testing")
      }
      ViewActions.closeSoftKeyboard()
      saveButton {
        assertIsEnabled()
        performClick()
      }
      verify { mockNavActions.goBack() }
      confirmVerified(mockNavActions)
    }
  }

  @Test
  fun inputDescription() {
    onComposeScreen<CreateToDoScreen>(composeTestRule) {
      todoNameField {
        performTextClearance()
        performTextInput("Official Task Testing")
      }
      todoDescriptionField {
        performTextClearance()
        performTextInput("Official Task Testing")
        assertTextContains("Official Task Testing")
      }
      ViewActions.closeSoftKeyboard()
      saveButton {
        assertIsEnabled()
        performClick()
      }
      verify { mockNavActions.goBack() }
      confirmVerified(mockNavActions)
    }
  }

  @Test
  fun datePickerTest() {
    onComposeScreen<CreateToDoScreen>(composeTestRule) {
      todoDateField { performClick() }
      datePicker { assertIsDisplayed() }
      dateConfirmButton {
        assertIsDisplayed()
        assertHasClickAction()
        assertTextEquals(("Confirm"))
      }
      dateDismissButton {
        assertIsDisplayed()
        assertHasClickAction()
        assertTextEquals(("Cancel"))
        performClick()
      }
      datePicker { assertIsNotDisplayed() }
    }
  }

  /*
  @Test
  fun inputDate() {
    onComposeScreen<CreateToDoScreen>(
      composeTestRule
    ) {
      todoDateField{
        performClick()
      }

      datePicker {
        PickerActions.setDate(2024,5,21)
      }
      //onView(withClassName(equalTo(DatePicker::class.java.name))).perform(PickerActions.setDate(2024, 5, 21))

      dateConfirmButton{
        performClick()
      }
      datePicker {
        assertIsNotDisplayed()
      }

      //composeTestRule.onNodeWithTag("todoDateField").assertTextContains("Tue May 21 2024")
      todoDateField{
        assertTextContains("")
      }


      todoNameField {
        performTextClearance()
        performTextInput("Official Task Testing")
      }
      ViewActions.closeSoftKeyboard()
      saveButton {
        assertIsEnabled()
        performClick()
      }
      verify { mockNavActions.goBack() }
      confirmVerified(mockNavActions)
    }
  }

   */

  @Test
  fun saveTaskDoesNotWorkWithEmptyTitle() = run {
    onComposeScreen<CreateToDoScreen>(composeTestRule) {
      step("Open createToDo screen") {
        todoNameField {
          assertIsDisplayed()
          // interact with the text field
          performClick()
          // clear the text field
          performTextClearance()
        }
        ViewActions.closeSoftKeyboard()

        saveButton {
          // arrange: verify pre-conditions
          assertIsDisplayed()
          assertIsNotEnabled()

          // act: click on the save button
          performClick()
        }
        // verify that the nav action has not been called
        verify { mockNavActions wasNot Called }
        confirmVerified(mockNavActions)
      }
    }
  }

  @Test
  fun elementsAreDisplayed() {
    onComposeScreen<CreateToDoScreen>(composeTestRule) {
      runBlocking {
        delay(6000) // Adjust the delay time as needed
      }
      createTodoCol { assertIsDisplayed() }
      todoNameField {
        assertIsDisplayed()
        assertHasClickAction()
      }
      todoDescriptionField {
        assertIsDisplayed()
        assertHasClickAction()
      }
      todoDateField {
        assertIsDisplayed()
        assertHasClickAction()
      }
      saveButton {
        assertIsDisplayed()
        assertHasClickAction()
        assertTextEquals(("Save"))
      }
    }
  }
}
