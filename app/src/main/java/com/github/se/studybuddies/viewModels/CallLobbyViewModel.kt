package com.github.se.studybuddies.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.DeviceStatus
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.model.StreamCallId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class CallLobbyViewModel(val uid: String) : ViewModel() {

  val callId: StreamCallId = StreamCallId.fromCallCid(uid)

  val call: Call by lazy {
    val streamVideo = StreamVideo.instance()
    val call = streamVideo.call(type = callId.type, id = callId.id)

    viewModelScope.launch {
      // create the call if it doesn't exist - this will also load the settings for the call,
      // this way the lobby screen can already display the right mic/camera settings
      // This also starts listening to the call events to get the participant count
      val callGetOrCreateResult = call.create()
      if (callGetOrCreateResult.isFailure) {
        // in demo we can ignore this. The lobby screen will just display default camera/video,
        // but we will show an error
        Log.e(
            "CallLobbyViewModel",
            "Failed to create the call ${callGetOrCreateResult.errorOrNull()}",
        )
        event.emit(CallLobbyEvent.JoinFailed(callGetOrCreateResult.errorOrNull()?.message))
      }
    }

    call
  }

  val cameraEnabled: StateFlow<Boolean> = call.camera.isEnabled
  val microphoneEnabled: StateFlow<Boolean> = call.microphone.isEnabled

  init {
    // for demo we set the default state for mic and camera to be on
    // and then we wait for call settings and we update the default state accordingly
    call.microphone.setEnabled(false)
    call.camera.setEnabled(false)

    viewModelScope.launch {
      // wait for settings (this will not block the UI) and then update the mic
      // based on it
      val settings = call.state.settings.first { it != null }
      val enabled =
          when (call.microphone.status.first()) {
            is DeviceStatus.NotSelected -> {
              settings?.audio?.micDefaultOn ?: false
            }
            is DeviceStatus.Enabled -> {
              true
            }
            is DeviceStatus.Disabled -> {
              false
            }
          }

      // enable/disable audi capture (audio indicator will not work otherwise)
      call.microphone.setEnabled(enabled)
    }

    viewModelScope.launch {
      // wait for settings (this will not block the UI) and then update the camera
      // based on it
      val settings = call.state.settings.first { it != null }

      val enabled =
          when (call.camera.status.first()) {
            is DeviceStatus.NotSelected -> {
              settings?.video?.cameraDefaultOn ?: false
            }
            is DeviceStatus.Enabled -> {
              true
            }
            is DeviceStatus.Disabled -> {
              false
            }
          }

      // enable/disable camera capture (no preview would be visible otherwise)
      call.camera.setEnabled(enabled)
    }
  }

  private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  internal val isLoading: StateFlow<Boolean> = _isLoading

  private val event: MutableSharedFlow<CallLobbyEvent> = MutableSharedFlow()
  internal val uiState: SharedFlow<CallLobbyUiState> =
      event
          .flatMapLatest { event ->
            when (event) {
              is CallLobbyEvent.JoinCall -> {
                flowOf(CallLobbyUiState.JoinCompleted)
              }
              is CallLobbyEvent.JoinFailed -> {
                flowOf(CallLobbyUiState.JoinFailed(event.reason))
              }
            }
          }
          .onCompletion { _isLoading.value = false }
          .shareIn(viewModelScope, SharingStarted.Lazily, 0)

  fun handleUiEvent(event: CallLobbyEvent) {
    viewModelScope.launch { this@CallLobbyViewModel.event.emit(event) }
  }

  fun enableCamera(enabled: Boolean) {
    call.camera.setEnabled(enabled)
  }

  fun enableMicrophone(enabled: Boolean) {
    call.microphone.setEnabled(enabled)
  }

  fun signOut() {
    viewModelScope.launch {
      StreamVideo.instance().logOut()
      StreamVideo.removeClient()
    }
  }
}

sealed interface CallLobbyUiState {
  data object Nothing : CallLobbyUiState

  data object JoinCompleted : CallLobbyUiState

  data class JoinFailed(val reason: String?) : CallLobbyUiState
}

sealed interface CallLobbyEvent {

  data object JoinCall : CallLobbyEvent

  data class JoinFailed(val reason: String?) : CallLobbyEvent
}
