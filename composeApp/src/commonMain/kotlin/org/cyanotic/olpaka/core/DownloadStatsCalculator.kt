package org.cyanotic.olpaka.core

import kotlinx.datetime.Clock

interface DownloadStatsCalculator {

    fun calculateDownloadSpeed(
        downloadedBytes: Long,
        previousBytesDownloaded: Long,
        previousUpdateTime: Long
    ): Long?

    fun calculateRemainingTime(
        totalBytes: Long,
        downloadedBytes: Long,
        previousBytesDownloaded: Long,
        previousUpdateTime: Long
    ): Long?
}

class DownloadStatsCalculatorDefault : DownloadStatsCalculator {

    override fun calculateDownloadSpeed(
        downloadedBytes: Long,
        previousBytesDownloaded: Long,
        previousUpdateTime: Long
    ): Long? {
        val timeElapsed = Clock.System.now().toEpochMilliseconds() - previousUpdateTime
        if (timeElapsed == 0L || downloadedBytes == previousBytesDownloaded) {
            return null
        }

        val speedBytesPerMillisecond = (downloadedBytes - previousBytesDownloaded) / timeElapsed
        return speedBytesPerMillisecond * 1000
    }

    override fun calculateRemainingTime(
        totalBytes: Long,
        downloadedBytes: Long,
        previousBytesDownloaded: Long,
        previousUpdateTime: Long
    ): Long? {

        val timeElapsed = Clock.System.now().toEpochMilliseconds() - previousUpdateTime
        if (timeElapsed == 0L || downloadedBytes == previousBytesDownloaded) {
            return null
        }

        val speedBytesPerMillisecond = (downloadedBytes - previousBytesDownloaded) / timeElapsed
        val speedBytesPerSecond = speedBytesPerMillisecond * 1000

        val remainingBytes = totalBytes - downloadedBytes
        if(speedBytesPerSecond == 0L){
            return null
        }
        val timeLeftSeconds = remainingBytes / speedBytesPerSecond
        return timeLeftSeconds
    }

}