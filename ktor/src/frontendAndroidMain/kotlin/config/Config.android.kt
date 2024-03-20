package config

import models.HostPort

// using ngrok to expose localhost to the internet
// ngrok: https://ngrok.com/
// cmd used: ngrok http http://localhost:8080
actual val serverConfig: HostPort
    get() = HostPort("https://a12d-2001-8a0-6c7c-4800-d01a-22d1-33c7-91a3.ngrok-free.app")