package org.cyanotic.olpaka.feature.models

import kotlin.math.round

interface DownloadStatsFormatter {

    fun formatDownloadSpeed(speedInBytesSecond: Long): String

    fun formatSizeInBytes(bytes: Long): String

    fun formatRemainingTime(seconds: Long): String

}

class DownloadStatsFormatterDefault : DownloadStatsFormatter {

    override fun formatDownloadSpeed(speedInBytesSecond: Long): String {
        val unit = getSuitableUnit(speedInBytesSecond)
        return formatBytes(speedInBytesSecond, unit) + "/s"
    }

    override fun formatSizeInBytes(bytes: Long): String {
        val unit = getSuitableUnit(bytes)
        return formatBytes(bytes, unit)
    }

    override fun formatRemainingTime(seconds: Long): String {
        return formatTime(seconds)
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
            "${s}s"
        }
    }

    private fun Double.toStringFormatted(): String {
        return (round(this * 10) / 10.0).toString()
    }

}
