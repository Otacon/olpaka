package org.cyanotic.olpaka

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Olpaka",
        state = WindowState(width = 1024.dp, height = 768.dp)
    ) {
        App()
    }
}