package org.cyanotic.olpaka.core

import kotlinx.coroutines.flow.MutableStateFlow
import org.cyanotic.olpaka.ui.theme.OlpakaColor
import org.cyanotic.olpaka.ui.theme.OlpakaTheme

class ThemeState{
    val color = MutableStateFlow(OlpakaColor.OLPAKA)
    val themeMode = MutableStateFlow(OlpakaTheme.AUTO)
}