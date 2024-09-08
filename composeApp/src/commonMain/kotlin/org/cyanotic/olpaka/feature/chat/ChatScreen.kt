package org.cyanotic.olpaka.feature.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.cyanotic.olpaka.ui.OlpakaAppBar
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun ChatScreen() {
    Scaffold(
        topBar = { OlpakaAppBar("Olpaka") },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text("Chat")
        }
    }

}