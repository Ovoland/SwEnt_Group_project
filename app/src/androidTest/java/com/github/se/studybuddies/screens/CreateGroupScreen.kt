package com.github.se.studybuddies.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasTestTag
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class CreateGroupScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<CreateGroupScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("create_group_scaffold") }) {

  // Structural elements of the UI
  val topAppBox: KNode = child { hasTestTag("top_app_box") }
  val topAppBar: KNode = topAppBox.child { hasTestTag("top_app_bar") }
  val goBackButton: KNode = topAppBar.child { hasTestTag("go_back_button") }
  val divider: KNode = onNode { hasTestTag("divider") }

  val createGroupCol: KNode = child { hasTestTag("create_group_column") }
  val groupField: KNode = createGroupCol.child { hasTestTag("group_field") }
  val groupFieldProposal: KNode = createGroupCol.child { hasClickAction() }
  val saveButton: KNode = onNode { hasTestTag("save_button") }
  val saveButtonText: KNode = onNode { hasTestTag("save_button_text") }
}