package android

import android.components.DisplayChatMessages
import android.components.SendMessageInput
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    messages: List<String>,
    onSendMessageRequest: (String) -> Unit,
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        DisplayChatMessages(modifier.weight(0.9f), messages = messages)
        SendMessageInput(modifier, onSendMessageRequest = onSendMessageRequest)
    }
}
