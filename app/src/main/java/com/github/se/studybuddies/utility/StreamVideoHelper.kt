package com.github.se.studybuddies.utility

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.github.se.studybuddies.database.DatabaseConnection
import com.google.firebase.auth.FirebaseAuth
import io.getstream.log.Priority
import io.getstream.video.android.core.BuildConfig
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.core.logging.LoggingLevel
import io.getstream.video.android.core.notifications.NotificationConfig
import io.getstream.video.android.datastore.delegate.StreamUserDataStore
import io.getstream.video.android.model.ApiKey
import io.getstream.video.android.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull

public enum class InitializedState {
    NOT_STARTED, RUNNING, FINISHED, FAILED
}

@SuppressLint("StaticFieldLeak")
object StreamVideoInitHelper {
    private lateinit var auth: FirebaseAuth

    private var isInitialising = false
    private lateinit var context: Context
    private val _initState = MutableStateFlow(InitializedState.NOT_STARTED)
    public val initializedState: StateFlow<InitializedState> = _initState

    fun init(appContext: Context) {
        context = appContext.applicationContext
    }

    suspend fun reloadSdk(
        dataStore: StreamUserDataStore,
        useRandomUserAsFallback: Boolean = true,
    ) {
        StreamVideo.removeClient()
        loadSdk()
    }

    /**
     * A helper function that will initialise the [StreamVideo] SDK and also the [ChatClient].
     * Set [useRandomUserAsFallback] to true if you want to use a guest fallback if the user is not
     * logged in.
     */
    suspend fun loadSdk() {
        if (StreamVideo.isInstalled) {
            _initState.value = InitializedState.FINISHED
            Log.w("StreamVideoInitHelper", "[initStreamVideo] StreamVideo is already initialised.")
            return
        }

        if (isInitialising) {
            _initState.value = InitializedState.RUNNING
            Log.d("StreamVideoInitHelper", "[initStreamVideo] StreamVideo is already initialising")
            return
        }

        isInitialising = true
        _initState.value = InitializedState.RUNNING

        auth = FirebaseAuth.getInstance()
        val loggedInUser = auth.currentUser
        val username = DatabaseConnection().getCurrentUser().username

        try {
            // If we have a logged in user (from the data store or randomly created above)
            // then we can initialise the SDK
            if (loggedInUser != null) {
                StreamVideoBuilder(
                    context = context,
                    apiKey = "x52wgjq8qyfc",
                    user = User(
                        id = loggedInUser.uid,
                        name = username,
                    ),
                    token =
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiSm9ydXVzX0NfQmFvdGgiLCJpc3MiOiJodHRwczovL3Byb250by5nZXRzdHJlYW0uaW8iLCJzdWIiOiJ1c2VyL0pvcnV1c19DX0Jhb3RoIiwiaWF0IjoxNzE0NjUzOTg0LCJleHAiOjE3MTUyNTg3ODl9.WkUHrFvbIdfjqKIcxi4FQB6GmQB1q0uyQEAfJ61P_g0"
                    ,
                    loggingLevel = LoggingLevel(priority = Priority.VERBOSE),
                )
            }
            Log.i("StreamVideoInitHelper", "Init successful.")
            _initState.value = InitializedState.FINISHED
        } catch (e: Exception) {
            _initState.value = InitializedState.FAILED
            Log.e("StreamVideoInitHelper", "Init failed.", e)
        }

        isInitialising = false
    }
}
