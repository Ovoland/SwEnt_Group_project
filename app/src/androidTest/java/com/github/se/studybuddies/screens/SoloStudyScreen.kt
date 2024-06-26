package com.github.se.studybuddies.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class SoloStudyScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<SoloStudyScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("Solo study_menu") }) {

  val drawerScaffold: KNode = onNode { hasTestTag("Solo study_drawer_scaffold") }
  val soloStudyScreen: KNode = drawerScaffold.child { hasTestTag("solo_study_home") }
  val row1: KNode = soloStudyScreen.child { hasTestTag("solo_study_row1") }
  val row2: KNode = soloStudyScreen.child { hasTestTag("solo_study_row2") }
  val flashCardButton: KNode = row1.child { hasTestTag("Flash Card_button") }
  val todoListButton: KNode = row1.child { hasTestTag("ToDo List_button") }
  val timerButton: KNode = row2.child { hasTestTag("Timer_button") }
  val calendarButton: KNode = row2.child { hasTestTag("Calendar_button") }

  val topAppBox: KNode = drawerScaffold.child { hasTestTag("Solo study_top_app_box") }
  val topAppBar: KNode = topAppBox.child { hasTestTag("Solo study_top_app_bar") }
  val drawerMenuButton: KNode = topAppBar.child { hasTestTag("drawer_menu_icon") }
  val drawerSheet: KNode = onNode { hasTestTag("Solo study_drawer_sheet") }
  val settingsButton: KNode = drawerSheet.child { hasTestTag("Settings_button") }
  val accountButton: KNode = drawerSheet.child { hasTestTag("Account_button") }
  val soloTitle: KNode = topAppBar.child { hasTestTag("main_title") }

  val soloBottomBar: KNode = drawerScaffold.child { hasTestTag("SoloStudyHome_bottom_nav_bar") }
  val soloStudyBottom: KNode = onNode { hasTestTag("Solo study_item") }
  val groupsBottom: KNode = onNode { hasTestTag("Groups_item") }
  val messagesBottom: KNode = onNode { hasTestTag("Messages_item") }
  val mapBottom: KNode = onNode { hasTestTag("Map_item") }
}
