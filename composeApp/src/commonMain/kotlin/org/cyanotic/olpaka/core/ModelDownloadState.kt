package org.cyanotic.olpaka.core

import kotlinx.coroutines.flow.MutableStateFlow

class ModelDownloadState {
    val currentDownloadState = MutableStateFlow(DownloadState.INACTIVE)

    fun setDownloading() {
        currentDownloadState.value = DownloadState.DOWNLOADING
    }

    fun setCompleted() {
        currentDownloadState.value = DownloadState.COMPLETED
    }

    fun setInactive() {
        currentDownloadState.value = DownloadState.INACTIVE
    }
}

enum class DownloadState {
    DOWNLOADING,
    COMPLETED,
    INACTIVE
}