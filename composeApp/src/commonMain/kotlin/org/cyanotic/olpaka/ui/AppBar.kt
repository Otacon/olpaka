package org.cyanotic.olpaka.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OlpakaAppBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(shadowElevation = 4.dp) {
        CenterAlignedTopAppBar(
            title = { Text(title) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
            ),
            actions = actions
        )
    }
}