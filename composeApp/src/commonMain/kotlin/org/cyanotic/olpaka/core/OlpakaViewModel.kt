package org.cyanotic.olpaka.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class OlpakaViewModel : ViewModel() {

    private var firstInit = false

    fun init() {
        if (!firstInit) {
            firstInit = true
            onCreate()
        }
    }

    open fun onCreate() {

    }

    protected fun inBackground(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            block()
        }
    }
}