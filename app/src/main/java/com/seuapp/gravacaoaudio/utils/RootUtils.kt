package com.seuapp.gravacaoaudio.utils

import android.util.Log
import java.io.DataOutputStream
import java.io.IOException

object RootUtils {
    private const val TAG = "RootUtils"

    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }

    private fun checkRootMethod1(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        return paths.any { path -> java.io.File(path).exists() }
    }

    private fun checkRootMethod2(): Boolean {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            return process.waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar root: ${e.message}")
            return false
        }
    }

    private fun checkRootMethod3(): Boolean {
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            return process.waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar root via execução de su: ${e.message}")
            return false
        }
    }

    fun hasRootPermission(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("echo 'test' > /system/etc/hosts\n")
            os.writeBytes("exit\n")
            os.flush()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao solicitar permissão root: ${e.message}")
            false
        } catch (e: InterruptedException) {
            Log.e(TAG, "Thread interrompida: ${e.message}")
            false
        }
    }

    fun executeRootCommand(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes(command + "\n")
            os.writeBytes("exit\n")
            os.flush()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao executar comando root: ${e.message}")
            false
        }
    }
}