package com.ubudy.voiceaikit.viewmodels

import android.app.Application
import android.media.AudioManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ubudy.voiceaikit.models.ConnectionState
import com.ubudy.voiceaikit.models.UserInfo
import com.ubudy.voiceaikit.models.VoiceAgentConfig
import com.ubudy.voiceaikit.services.AudioAnalyzer
import com.ubudy.voiceaikit.services.TokenService
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.room.track.AudioTrack
import io.livekit.android.room.track.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceAgentViewModel(
    application: Application,
    private val config: VoiceAgentConfig
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ConnectionState.IDLE)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    var userInfo = UserInfo()

    private var room: io.livekit.android.room.Room? = null
    private val agentAudioAnalyzer = AudioAnalyzer()
    private var timerJob: Job? = null
    private var eventCollectionJob: Job? = null
    private var audioLevelJob: Job? = null
    private var speakingHoldFrames = 0

    fun toggle() {
        if (_state.value == ConnectionState.CONNECTING) return
        if (_state.value == ConnectionState.IDLE || _state.value == ConnectionState.DISCONNECTED) {
            connect()
        } else {
            disconnect()
        }
    }

    private fun connect() {
        viewModelScope.launch {
            _state.value = ConnectionState.CONNECTING

            for (attempt in 1..config.maxRetries) {
                try {
                    val tokenResponse = TokenService.fetchToken(config.serverURL, userInfo)

                    val audioManager = getApplication<Application>()
                        .getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
                    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                    audioManager.isSpeakerphoneOn = true

                    val newRoom = LiveKit.create(getApplication())
                    room = newRoom

                    eventCollectionJob = viewModelScope.launch {
                        newRoom.events.events.collect { event -> handleEvent(event) }
                    }

                    newRoom.connect(config.livekitURL, tokenResponse.token)
                    newRoom.localParticipant.setMicrophoneEnabled(true)

                    _state.value = ConnectionState.LISTENING
                    startTimer()
                    return@launch
                } catch (e: Exception) {
                    Log.e(TAG, "Attempt $attempt failed: ${e.message}")
                    cleanUp()
                    if (attempt < config.maxRetries) delay(1000)
                }
            }

            _state.value = ConnectionState.DISCONNECTED
        }
    }

    private fun handleEvent(event: RoomEvent) {
        when (event) {
            is RoomEvent.TrackSubscribed -> {
                if (event.track is AudioTrack) {
                    agentAudioAnalyzer.attachToTrack(event.track as AudioTrack)
                    startAudioLevelMonitoring()
                }
            }
            is RoomEvent.Disconnected -> {
                cleanUp()
                _state.value = ConnectionState.DISCONNECTED
            }
            is RoomEvent.ActiveSpeakersChanged -> {
                val agentSpeaking = event.speakers.any {
                    it.identity?.value?.startsWith("agent-") == true
                }
                if (_state.value != ConnectionState.CONNECTING &&
                    _state.value != ConnectionState.IDLE &&
                    _state.value != ConnectionState.DISCONNECTED
                ) {
                    if (agentSpeaking) {
                        speakingHoldFrames = 25
                        if (_state.value != ConnectionState.SPEAKING) {
                            _state.value = ConnectionState.SPEAKING
                            _audioLevel.value = 0.5f
                        }
                    } else if (speakingHoldFrames > 0) {
                        speakingHoldFrames--
                    } else if (_state.value != ConnectionState.LISTENING) {
                        _state.value = ConnectionState.LISTENING
                        _audioLevel.value = 0f
                    }
                }
            }
            else -> {}
        }
    }

    private fun startAudioLevelMonitoring() {
        audioLevelJob?.cancel()
        audioLevelJob = viewModelScope.launch {
            agentAudioAnalyzer.level.collect { level ->
                _audioLevel.value = level
                if (_state.value != ConnectionState.CONNECTING) {
                    if (level > 0.06f) {
                        speakingHoldFrames = 25
                        if (_state.value != ConnectionState.SPEAKING) {
                            _state.value = ConnectionState.SPEAKING
                        }
                    } else if (speakingHoldFrames > 0) {
                        speakingHoldFrames--
                    } else if (_state.value != ConnectionState.LISTENING) {
                        _state.value = ConnectionState.LISTENING
                    }
                }
            }
        }
    }

    private fun startTimer() {
        _elapsedSeconds.value = 0
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            cleanUp()
            _state.value = ConnectionState.IDLE
        }
    }

    private fun cleanUp() {
        timerJob?.cancel(); timerJob = null
        eventCollectionJob?.cancel(); eventCollectionJob = null
        audioLevelJob?.cancel(); audioLevelJob = null
        agentAudioAnalyzer.detach()
        speakingHoldFrames = 0
        _audioLevel.value = 0f
        room?.disconnect()
        room = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanUp()
    }

    companion object {
        private const val TAG = "VoiceAIKit"
    }
}

class VoiceAgentViewModelFactory(
    private val application: Application,
    private val config: VoiceAgentConfig
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VoiceAgentViewModel(application, config) as T
    }
}
