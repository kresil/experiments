package android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ChatActivity : ComponentActivity() {

    private val viewModel by viewModels<ChatViewModel> {
        ChatViewModel.factory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            viewModel.initConnection()
        }
        setContent {
            val messages by viewModel.stateFlow.collectAsState(initial = emptyList())
            ChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(
                        onSendMessageRequest = { viewModel.sendMessage(it) },
                        messages = messages
                    )
                }
            }
        }
    }
}
