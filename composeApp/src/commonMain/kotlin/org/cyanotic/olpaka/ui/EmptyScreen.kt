package org.cyanotic.olpaka.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown

@Composable
fun EmptyScreen(
    modifier: Modifier,
    title: String,
    subtitle: String,
    cta: @Composable ColumnScope.() -> Unit = {},
) {
    Box(modifier) {
        Card(
            modifier = Modifier.align(Alignment.Center),
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Markdown(
                    content = subtitle,
                    modifier = Modifier
                )
                Spacer(Modifier.height(16.dp))
                cta()
            }
        }
    }
}