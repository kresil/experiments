package android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import config.wsClient
import connection.ChatConnHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private typealias Messages = List<String>

class ChatViewModel : ViewModel() {
    companion object {
        fun factory() = viewModelFactory {
            initializer { ChatViewModel() }
        }
    }

    val stateFlow: Flow<Messages>
        get() = _stateFlow

    private val _stateFlow: MutableStateFlow<Messages> =
        MutableStateFlow(listOf("hello from android"))

    private val wsClientAndroid = wsClient

    fun initConnection() {
        viewModelScope.launch {
            _stateFlow.value += ChatConnHandler.testConnection()
            ChatConnHandler.initConnection(
                this,
                wsClient = wsClientAndroid,
                appendMessage = { writeMessage(it) }
            )
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            if (message.isNotEmpty()) {
                writeMessage("You: $message")
                wsClientAndroid.send(message)
            }
        }
    }

    private fun writeMessage(message: String) {
        _stateFlow.value += message
    }

}
