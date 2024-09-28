package org.cyanotic.olpaka

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.desktop_icon
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource(Res.drawable.desktop_icon),
        title = "Olpaka",
        state = WindowState(width = 1024.dp, height = 768.dp),
    ) {
        App()
    }
}