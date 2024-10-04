package org.cyanotic.olpaka.core

import kotlinx.coroutines.flow.MutableStateFlow

interface ModelDownloadState {
    val currentDownloadState: MutableStateFlow<DownloadState>
    fun setDownloading()
    fun setCompleted()
    fun setInactive()
}

class ModelDownloadStateDefault : ModelDownloadState {
    override val currentDownloadState = MutableStateFlow(DownloadState.INACTIVE)

    override fun setDownloading() {
        currentDownloadState.value = DownloadState.DOWNLOADING
    }

    override fun setCompleted() {
        currentDownloadState.value = DownloadState.COMPLETED
    }

    override fun setInactive() {
        currentDownloadState.value = DownloadState.INACTIVE
    }
}

enum class DownloadState {
    DOWNLOADING,
    COMPLETED,
    INACTIVE
}