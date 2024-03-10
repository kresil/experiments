package application

import application.models.ChatSession
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

class WebSocketServerHandler(val server: ChatServer) {

    suspend fun DefaultWebSocketServerSession.handleChatSession() {
        // First of all, we get the session.
        val session = call.sessions.get<ChatSession>()

        // We check that we actually have a session. We should always have one,
        // since we have defined an interceptor before to set one.
        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
            return
        }

        // We notify that a member joined by calling the server handler [memberJoin].
        // This allows associating the session ID to a specific WebSocket connection.
        server.memberJoin(session.id, this)

        try {
            // We start receiving messages (frames).
            // Since this is a coroutine, it is suspended until receiving frames.
            // Once the connection is closed, this consumeEach will finish and the code will continue.
            incoming.consumeEach { frame ->
                // Frames can be [Text], [Binary], [Ping], [Pong], [Close].
                // We are only interested in textual messages, so we filter it.
                if (frame is Frame.Text) {
                    // Now it is time to process the text sent from the user.
                    // At this point, we have context about this connection,
                    // the session, the text and the server.
                    // So we have everything we need.
                    receivedMessage(session.id, frame.readText())
                }
            }
        } finally {
            // Either if there was an error, or if the connection was closed gracefully,
            // we notified the server that the member had left.
            server.memberLeft(session.id, this)
        }
    }

    /**
     * We received a message. Let's process it.
     */
    private suspend fun receivedMessage(id: String, command: String) =
        // We are going to handle commands (text starting with '/') and normal messages
        when {
            // The command `who` responds the user about all the member names connected to the user.
            command.startsWith("/who") -> server.who(id)
            // The command `user` allows the user to set its name.
            command.startsWith("/user") -> {
                // We strip the command part to get the rest of the parameters.
                // In this case the only parameter is the user's newName.
                val newName = command.removePrefix("/user").trim()
                // We verify that it is a valid name (in terms of length) to prevent abusing
                when {
                    newName.isEmpty() -> server.sendTo(id, "server::help", "/user [newName]")
                    newName.length > 50 -> server.sendTo(
                        id,
                        "server::help",
                        "new name is too long: 50 characters limit"
                    )

                    else -> server.memberRenamed(id, newName)
                }
            }
            // The command 'help' allows users to get a list of available commands.
            command.startsWith("/help") -> server.help(id)
            // If no commands are matched at this point, we notify about it.
            command.startsWith("/") -> server.sendTo(
                id,
                "server::help",
                "Unknown command ${command.takeWhile { !it.isWhitespace() }}"
            )
            // Handle a normal message.
            else -> server.message(id, command)
        }
}
