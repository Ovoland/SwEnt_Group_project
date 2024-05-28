package com.github.se.studybuddies.ui.video_call

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.studybuddies.R
import com.github.se.studybuddies.navigation.NavigationActions
import com.github.se.studybuddies.navigation.Route
import com.github.se.studybuddies.ui.theme.Blue
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.activecall.CallContent
import io.getstream.video.android.compose.ui.components.call.controls.ControlActions
import io.getstream.video.android.compose.ui.components.call.controls.actions.DefaultOnCallActionHandler
import io.getstream.video.android.compose.ui.components.call.renderer.LayoutType
import io.getstream.video.android.compose.ui.components.call.renderer.ParticipantVideo
import io.getstream.video.android.compose.ui.components.call.renderer.ParticipantsLayout
import io.getstream.video.android.core.call.state.LeaveCall

// Design UI elements using Jetpack Compose
@Composable
fun VideoCallScreen(
    callId: String,
    state: VideoCallState,
    onAction: (VideoCallAction) -> Unit,
    navigationActions: NavigationActions
) {

  VideoTheme {
    when {
      state.error != null -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(text = state.error, color = MaterialTheme.colorScheme.error)
        }
      }
      state.callState == CallState.JOINING -> {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
              CircularProgressIndicator(color = Blue)
              Text(text = "Joining...")
            }
      }
      else -> {
        val layout by remember { mutableStateOf(LayoutType.DYNAMIC) }
        val speakingWhileMuted by state.call.state.speakingWhileMuted.collectAsState()
        if (speakingWhileMuted) {
          Toast.makeText(LocalContext.current, R.string.speaking_while_muted, Toast.LENGTH_SHORT)
              .show()
        }
        val context = LocalContext.current
        Column(modifier = Modifier.fillMaxSize().testTag("video_call_screen")) {
          CallContent(
              modifier = Modifier.fillMaxSize().background(color = Blue).testTag("call_content"),
              call = state.call,
              enableInPictureInPicture = true,
              isShowingOverlayAppBar = false,
              layout = layout,
              videoContent = {
                ParticipantsLayout(
                    call = state.call,
                    modifier = Modifier.fillMaxSize().weight(1f),
                    videoRenderer = { modifier, _, participant, style ->
                      ParticipantVideo(
                          call = state.call,
                          participant = participant,
                          style = style,
                          modifier = modifier.padding(4.dp).clip(RoundedCornerShape(8.dp)))
                    },
                )
              },
              controlsContent = {
                ControlActions(
                    call = state.call,
                    modifier = Modifier.testTag("control_actions"),
                    onCallAction = { action ->
                      when (action) {
                        is LeaveCall -> onAction(VideoCallAction.LeaveCall)
                        else -> {
                          DefaultOnCallActionHandler.onCallAction(state.call, action)
                        }
                      }
                    })
                BackHandler { navigationActions.navigateTo("${Route.GROUP}/${callId}") }
              },
          )
        }
      }
    }
  }
}
