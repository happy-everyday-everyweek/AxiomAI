package com.ai.assistance.operit.util

import com.ai.assistance.operit.core.tools.FFmpegResultData

/**
 * Utility class for FFmpeg operations
 */
object FFmpegUtil {
    private const val TAG = "FFmpegUtil"

    /**
     * Build a scale filter string that survives FFmpeg argument parsing.
     * FFmpeg expressions need an escaped comma when passed without a shell.
     */
    fun scaleFilterMaxWidth(maxWidth: Int): String = "scale=min(${maxWidth}\\,iw):-2"

    /**
     * Execute an FFmpeg command and return if it was successful
     */
    fun executeCommand(command: String): Boolean {
        try {
            AppLogger.d(TAG, "Executing FFmpeg command: $command")
            val parts = mutableListOf("ffmpeg")
            parts.addAll(command.split("\\s+".toRegex()).filter { it.isNotBlank() })
            val process = Runtime.getRuntime().exec(parts.toTypedArray())
            val returnCode = process.waitFor()

            if (returnCode == 0) {
                AppLogger.d(TAG, "FFmpeg command executed successfully")
                return true
            } else {
                val output = process.errorStream.bufferedReader().readText()
                AppLogger.e(
                    TAG,
                    "FFmpeg failed with return code: $returnCode, output: $output"
                )
                return false
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error executing FFmpeg command", e)
            return false
        }
    }

    /**
     * Get media information for a file using ffprobe
     */
    fun getMediaInfo(filePath: String): FFmpegResultData.MediaInfo? {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("ffprobe", "-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", filePath)
            )
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            parseMediaInfoFromJson(output)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error getting media info: ${e.message}")
            null
        }
    }

    private fun parseMediaInfoFromJson(json: String): FFmpegResultData.MediaInfo? {
        try {
            val jsonObject = org.json.JSONObject(json)

            val formatObj = jsonObject.optJSONObject("format") ?: return null
            val formatName = formatObj.optString("format_name", "unknown")
            val duration = formatObj.optString("duration", "0")
            val bitrate = formatObj.optString("bit_rate", "0")

            val streamsArray = jsonObject.optJSONArray("streams") ?: return null
            val videoStreams = mutableListOf<FFmpegResultData.StreamInfo>()
            val audioStreams = mutableListOf<FFmpegResultData.StreamInfo>()

            for (i in 0 until streamsArray.length()) {
                val streamObj = streamsArray.getJSONObject(i)
                val codecType = streamObj.optString("codec_type", "unknown")
                val index = streamObj.optInt("index", 0)
                val codecName = streamObj.optString("codec_name", "unknown")

                when (codecType) {
                    "video" -> {
                        val width = streamObj.optInt("width", 0)
                        val height = streamObj.optInt("height", 0)
                        val resolution = if (width > 0 && height > 0) "${width}x${height}" else null
                        val frameRate = streamObj.optString("r_frame_rate", null)
                        videoStreams.add(FFmpegResultData.StreamInfo(
                            index = index,
                            codecType = codecType,
                            codecName = codecName,
                            resolution = resolution,
                            frameRate = frameRate
                        ))
                    }
                    "audio" -> {
                        val sampleRate = streamObj.optString("sample_rate", null)
                        val channels = streamObj.optInt("channels", 0).let { if (it > 0) it else null }
                        audioStreams.add(FFmpegResultData.StreamInfo(
                            index = index,
                            codecType = codecType,
                            codecName = codecName,
                            sampleRate = sampleRate,
                            channels = channels
                        ))
                    }
                }
            }

            return FFmpegResultData.MediaInfo(
                format = formatName,
                duration = duration,
                bitrate = bitrate,
                videoStreams = videoStreams,
                audioStreams = audioStreams
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error parsing media info JSON: ${e.message}")
            return null
        }
    }
}
