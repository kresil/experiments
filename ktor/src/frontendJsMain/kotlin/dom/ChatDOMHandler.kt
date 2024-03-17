package connection.dom

import connection.WsClient
import kotlinx.browser.document
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent

/**
 * Handles the DOM events and updates the UI.
 */
object ChatDOMHandler {
    @OptIn(DelicateCoroutinesApi::class)
    fun setupEventListeners(wsClient: WsClient) {
        document.addEventListener("DOMContentLoaded", {
            val sendButton = document.getElementById("sendButton") as HTMLElement
            val commandInput = document.getElementById("commandInput") as HTMLInputElement

            sendButton.addEventListener("click", {
                GlobalScope.launch { sendMessageAndClearInput(wsClient, commandInput) }
            })
            commandInput.addEventListener("keydown", { e ->
                if ((e as KeyboardEvent).key == "Enter") {
                    GlobalScope.launch { sendMessageAndClearInput(wsClient, commandInput) }
                }
            })
        })
    }

    /**
     * Sends a message to the chat and clears the input.
     */
    private suspend fun sendMessageAndClearInput(client: WsClient, input: HTMLInputElement) {
        if (input.value.isNotEmpty()) {
            client.send(input.value)
            input.value = ""
        }
    }

    /**
     * Appends a message to the chat.
     */
    fun appendMessage(message: String) {
        val line = document.createElement("p") as HTMLElement
        line.className = "message"
        line.textContent = message

        val messagesBlock = document.getElementById("messages") as HTMLElement
        messagesBlock.appendChild(line)
        messagesBlock.scrollTop = line.offsetTop.toDouble()
    }
}