package android.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DisplayChatMessages(messages: List<String>) =
    LazyColumn {
        items(messages.size) { index ->
            Text(messages[index])
        }
    }
