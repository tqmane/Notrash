package com.notrash.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object ShellUtils {
    sealed class Result {
        object Success : Result()
        data class Error(val message: String) : Result()
        object PermissionDenied : Result()
    }

    suspend fun restartTargetApps(): Result = runRootCommand(
        "am force-stop com.nothing.camera && am force-stop com.nothing.ntessentialrecorder && " +
            "am force-stop com.nothing.ntessentialspace"
    )

    suspend fun runRootCommand(command: String): Result = withContext(Dispatchers.IO) {
        var process: Process? = null
        try {
            process = ProcessBuilder("su", "-c", command).redirectErrorStream(true).start()
            process.outputStream.close()
            if (!process.waitFor(15, TimeUnit.SECONDS)) {
                Result.Error("Root command timed out")
            } else if (process.exitValue() == 0) {
                Result.Success
            } else {
                Result.Error(process.inputStream.bufferedReader().readText().take(300))
            }
        } catch (error: java.io.IOException) {
            Result.PermissionDenied
        } finally {
            process?.destroy()
        }
    }
}
