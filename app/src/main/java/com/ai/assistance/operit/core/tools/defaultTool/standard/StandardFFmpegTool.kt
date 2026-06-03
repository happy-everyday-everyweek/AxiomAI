package com.ai.assistance.operit.core.tools.defaultTool.standard

import android.content.Context
import com.ai.assistance.operit.core.tools.FFmpegResultData
import com.ai.assistance.operit.core.tools.StringResultData
import com.ai.assistance.operit.core.tools.ToolExecutor
import com.ai.assistance.operit.data.model.AITool
import com.ai.assistance.operit.data.model.ToolResult
import com.ai.assistance.operit.data.model.ToolValidationResult
import com.ai.assistance.operit.util.AppLogger
import java.io.File

/** FFmpeg工具执行器 提供媒体文件处理能力，包括转换、裁剪、合并等功能 */
class StandardFFmpegToolExecutor(private val context: Context) : ToolExecutor {
    companion object {
        private const val TAG = "FFmpegToolExecutor"
    }

    override fun invoke(tool: AITool): ToolResult {
        val command = tool.parameters.find { it.name == "command" }?.value ?: ""

        if (command.isEmpty()) {
            return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Command cannot be empty"
            )
        }

        return try {
            val startTime = System.currentTimeMillis()

            val process = Runtime.getRuntime().exec(arrayOf("ffmpeg", *command.split("\\s+".toRegex()).toTypedArray()))
            val output = process.inputStream.bufferedReader().readText() +
                    process.errorStream.bufferedReader().readText()
            val returnCode = process.waitFor()
            val duration = System.currentTimeMillis() - startTime

            if (returnCode == 0) {
                ToolResult(
                        toolName = tool.name,
                        success = true,
                        result =
                                FFmpegResultData(
                                        command = command,
                                        returnCode = returnCode,
                                        output = output,
                                        duration = duration
                                )
                )
            } else {
                ToolResult(
                        toolName = tool.name,
                        success = false,
                        result = StringResultData(""),
                        error = "FFmpeg execution failed, return code: $returnCode\nOutput:\n$output"
                )
            }
        } catch (e: Exception) {
            ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "FFmpeg execution exception: ${e.message}"
            )
        }
    }

    override fun validateParameters(tool: AITool): ToolValidationResult {
        val command = tool.parameters.find { it.name == "command" }?.value

        if (command.isNullOrEmpty()) {
            return ToolValidationResult(valid = false, errorMessage = "Must provide command parameter")
        }

        return ToolValidationResult(valid = true)
    }
}

/** FFmpeg信息工具执行器 获取有关系统FFmpeg配置的信息 */
class StandardFFmpegInfoToolExecutor : ToolExecutor {
    companion object {
        private const val TAG = "FFmpegInfoToolExecutor"
    }

    override fun invoke(tool: AITool): ToolResult {
        return try {
            val info = StringBuilder()
            val startTime = System.currentTimeMillis()

            val process = Runtime.getRuntime().exec(arrayOf("ffmpeg", "-version"))
            val versionOutput = process.inputStream.bufferedReader().readText() +
                    process.errorStream.bufferedReader().readText()
            process.waitFor()
            info.appendLine("FFmpeg version info:")
            info.appendLine(versionOutput)

            val codecsProcess = Runtime.getRuntime().exec(arrayOf("ffmpeg", "-codecs"))
            val codecsOutput = codecsProcess.inputStream.bufferedReader().readText() +
                    codecsProcess.errorStream.bufferedReader().readText()
            codecsProcess.waitFor()
            val duration = System.currentTimeMillis() - startTime

            info.appendLine("\nSupported codecs:")
            info.appendLine(codecsOutput)

            ToolResult(
                    toolName = tool.name,
                    success = true,
                    result =
                            FFmpegResultData(
                                    command = "-codecs",
                                    returnCode = 0,
                                    output = info.toString(),
                                    duration = duration
                            )
            )
        } catch (e: Exception) {
            ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Failed to get FFmpeg info: ${e.message}"
            )
        }
    }

    override fun validateParameters(tool: AITool): ToolValidationResult {
        // 不需要参数
        return ToolValidationResult(valid = true)
    }
}

/** FFmpeg转换视频工具执行器 提供一个简化的接口用于常见的视频转换操作 */
class StandardFFmpegConvertToolExecutor(private val context: Context) : ToolExecutor {
    companion object {
        private const val TAG = "FFmpegConvertToolExecutor"
    }

    override fun invoke(tool: AITool): ToolResult {
        val inputPath = tool.parameters.find { it.name == "input_path" }?.value ?: ""
        val outputPath = tool.parameters.find { it.name == "output_path" }?.value ?: ""
        val format = tool.parameters.find { it.name == "format" }?.value
        val resolution = tool.parameters.find { it.name == "resolution" }?.value
        val bitrate = tool.parameters.find { it.name == "bitrate" }?.value
        val audioCodec = tool.parameters.find { it.name == "audio_codec" }?.value
        val videoCodec = tool.parameters.find { it.name == "video_codec" }?.value

        if (inputPath.isEmpty() || outputPath.isEmpty()) {
            return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Input path and output path cannot be empty"
            )
        }

        val inputFile = File(inputPath)
        if (!inputFile.exists()) {
            return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Input file does not exist: $inputPath"
            )
        }

        // 构建FFmpeg命令
        val commandParts = mutableListOf("ffmpeg", "-i", inputPath)

        // 添加可选参数
        if (!videoCodec.isNullOrEmpty()) {
            commandParts.addAll(listOf("-c:v", videoCodec))
        }

        if (!audioCodec.isNullOrEmpty()) {
            commandParts.addAll(listOf("-c:a", audioCodec))
        }

        if (!resolution.isNullOrEmpty()) {
            commandParts.addAll(listOf("-s", resolution))
        }

        if (!bitrate.isNullOrEmpty()) {
            commandParts.addAll(listOf("-b:v", bitrate))
        }

        // 添加输出文件
        commandParts.add(outputPath)

        val command = commandParts.joinToString(" ")

        return try {
            val startTime = System.currentTimeMillis()

            val process = Runtime.getRuntime().exec(commandParts.toTypedArray())
            val output = process.inputStream.bufferedReader().readText() +
                    process.errorStream.bufferedReader().readText()
            val returnCode = process.waitFor()
            val duration = System.currentTimeMillis() - startTime

            if (returnCode == 0) {
                val ffmpegResult =
                    try {
                        val mediaInfo = com.ai.assistance.operit.util.FFmpegUtil.getMediaInfo(outputPath)
                        if (mediaInfo != null) {
                            FFmpegResultData(
                                    command = command,
                                    returnCode = returnCode,
                                    output = output,
                                    duration = duration,
                                    outputFile = outputPath,
                                    mediaInfo = mediaInfo
                            )
                        } else {
                            FFmpegResultData(
                                    command = command,
                                    returnCode = returnCode,
                                    output = output,
                                    duration = duration,
                                    outputFile = outputPath
                            )
                        }
                    } catch (e: Exception) {
                        AppLogger.e(TAG, "Failed to get media info for output file", e)
                        FFmpegResultData(
                                command = command,
                                returnCode = returnCode,
                                output = output,
                                duration = duration,
                                outputFile = outputPath
                        )
                    }

                ToolResult(toolName = tool.name, success = true, result = ffmpegResult)
            } else {
                ToolResult(
                        toolName = tool.name,
                        success = false,
                        result = StringResultData(""),
                        error = "Video conversion failed, return code: $returnCode\nCommand: $command\nOutput:\n$output"
                )
            }
        } catch (e: Exception) {
            ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Video conversion exception: ${e.message}\nCommand: $command"
            )
        }
    }

    override fun validateParameters(tool: AITool): ToolValidationResult {
        val inputPath = tool.parameters.find { it.name == "input_path" }?.value
        val outputPath = tool.parameters.find { it.name == "output_path" }?.value

        if (inputPath.isNullOrEmpty()) {
            return ToolValidationResult(valid = false, errorMessage = "Must provide input_path parameter")
        }

        if (outputPath.isNullOrEmpty()) {
            return ToolValidationResult(valid = false, errorMessage = "Must provide output_path parameter")
        }

        return ToolValidationResult(valid = true)
    }
}
