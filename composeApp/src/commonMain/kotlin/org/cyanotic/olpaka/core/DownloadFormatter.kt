package org.cyanotic.olpaka.core

import kotlinx.datetime.Clock
import kotlin.math.round

interface DownloadFormatter {
    fun formatDownloadProgress(
        totalBytes: Long,
        downloadedBytes: Long,
        previousBytesDownloaded: Long,
        previousUpdateTime: Long
    ): String
}

class DownloadFormatterDefault : DownloadFormatter {

    override fun formatDownloadProgress(
        totalBytes: Long,
        downloadedBytes: Long,
        previousBytesDownloaded: Long,
        previousUpdateTime: Long
    ): String {

        val timeElapsed = Clock.System.now().toEpochMilliseconds() - previousUpdateTime
        if (timeElapsed == 0L || downloadedBytes == previousBytesDownloaded) {
            return "Calculating..."
        }

        val speedBytesPerMillisecond = (downloadedBytes - previousBytesDownloaded) / timeElapsed
        val speedBytesPerSecond = speedBytesPerMillisecond * 1000

        val remainingBytes = totalBytes - downloadedBytes
        if(speedBytesPerSecond == 0L){
            return "Calculating..."
        }
        val timeLeftSeconds = remainingBytes / speedBytesPerSecond

        val sizeUnit = getSuitableUnit(totalBytes)
        val speedUnit = getSuitableUnit(speedBytesPerSecond)

        val totalSizeFormatted = formatBytes(totalBytes, sizeUnit)
        val downloadedSizeFormatted = formatBytes(downloadedBytes, sizeUnit)

        val speedFormatted = formatBytes(speedBytesPerSecond, speedUnit)

        val timeLeftFormatted = formatTime(timeLeftSeconds)

        return "$speedFormatted/s - $downloadedSizeFormatted of $totalSizeFormatted, $timeLeftFormatted left"
    }

    private fun getSuitableUnit(bytes: Long): String {
        return when {
            bytes >= 1L * 1024 * 1024 * 1024 -> "GB"
            bytes >= 1L * 1024 * 1024 -> "MB"
            bytes >= 1L * 1024 -> "KB"
            else -> "B"
        }
    }

    private fun formatBytes(bytes: Long, unit: String): String {
        val decimalBytes = bytes.toDouble()
        val formattedValue = when (unit) {
            "GB" -> decimalBytes / 1024 / 1024 / 1024
            "MB" -> decimalBytes / 1024 / 1024
            "KB" -> decimalBytes / 1024
            else -> decimalBytes
        }.toStringFormatted()

        return "$formattedValue $unit"
    }

    private fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60

        return if (h > 0) {
            "${h}h ${m}m ${s}s"
        } else if (m > 0) {
            "${m}m ${s}s"
        } else {
            "$s s"
        }
    }

    private fun Double.toStringFormatted() : String {
        return (round(this * 10) / 10.0).toString()
    }

}