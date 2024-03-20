package android.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DisplayChatMessages(modifier: Modifier, messages: List<String>) =
    LazyColumn(modifier) {
        items(messages.size) { index ->
            Text(messages[index])
        }
    }
