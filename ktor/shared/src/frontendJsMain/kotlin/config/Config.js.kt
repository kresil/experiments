package config

import kotlinx.browser.window
import models.HostPort

actual val serverConfig: HostPort
    get() = HostPort(window.location.hostname, window.location.port.toInt())