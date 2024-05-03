package com.github.se.studybuddies.ui.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.studybuddies.R
import com.github.se.studybuddies.ui.theme.Blue
import com.github.se.studybuddies.ui.theme.Red
import com.github.se.studybuddies.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupFields(nameState: MutableState<String>) {
  Spacer(Modifier.height(20.dp))
  OutlinedTextField(
      value = nameState.value,
      onValueChange = { nameState.value = it },
      label = { Text(stringResource(R.string.group_name), color = Blue) },
      placeholder = { Text(stringResource(R.string.enter_a_group_name), color = Blue) },
      singleLine = true,
      modifier =
          Modifier.padding(0.dp)
              .width(300.dp)
              .height(65.dp)
              .clip(MaterialTheme.shapes.small)
              .testTag("group_name_field"),
      colors =
          TextFieldDefaults.outlinedTextFieldColors(
              focusedBorderColor = Blue, unfocusedBorderColor = Blue, cursorColor = Blue))
}

@Composable
fun SaveButton(nameState: MutableState<String>, save: () -> Unit) {
  Button(
      onClick = save,
      enabled = nameState.value.isNotBlank(),
      modifier =
          Modifier.padding(20.dp)
              .width(300.dp)
              .height(50.dp)
              .background(color = Blue, shape = RoundedCornerShape(size = 10.dp))
              .testTag("save_button"),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = Blue,
          )) {
        Text(
            stringResource(R.string.save),
            color = White,
            modifier = Modifier.testTag("save_button_text"))
      }
}

@Composable
fun GroupTitle(title: String) {
  Text(title, modifier = Modifier.padding(0.dp).testTag("todoTitle"), color = Red)
}
