package org.cyanotic.olpaka.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.ln
import kotlin.math.pow


fun ViewModel.inBackground(block: suspend CoroutineScope.() -> Unit) {
    viewModelScope.launch(Dispatchers.Default){
        block()
    }
}

fun Long.toHumanReadableByteCount(): String {
    val unit = 1000
    if (this < unit) return "$this B"
    val exp = (ln(this.toDouble()) / ln(unit.toDouble())).toInt()
    val prefixes = "kMGTPE"
    val pre = prefixes[exp - 1]
    val result = this / unit.toDouble().pow(exp)
    val roundedResult = (result * 10).toInt() / 10.0
    return "$roundedResult ${pre}B"
}