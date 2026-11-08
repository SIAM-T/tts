package com.example.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import java.security.MessageDigest
import java.util.concurrent.Executors

class SecurityManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("hisab_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_ENABLED = "key_pin_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_CURRENCY = "key_currency"
        private const val SALT = "hisab_store_salt_2026"
    }

    fun isPinEnabled(): Boolean {
        return prefs.getBoolean(KEY_PIN_ENABLED, false) && prefs.getString(KEY_PIN_HASH, null) != null
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
    }

    fun disablePin() {
        prefs.edit()
            .putBoolean(KEY_PIN_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    fun removePin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    fun verifyPin(inputPin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return true
        return hashPin(inputPin) == storedHash
    }

    fun getCurrency(): String {
        return prefs.getString(KEY_CURRENCY, "$") ?: "$"
    }

    fun setCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun canUseBiometrics(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val biometricManager = context.getSystemService(android.hardware.biometrics.BiometricManager::class.java)
            if (biometricManager != null) {
                val canAuth = biometricManager.canAuthenticate(android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_WEAK)
                return canAuth == android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS
            }
        }
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return keyguard?.isDeviceSecure == true
    }

    fun triggerBiometricPrompt(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val executor = Executors.newSingleThreadExecutor()
            val cancellationSignal = CancellationSignal()

            val prompt = BiometricPrompt.Builder(activity)
                .setTitle("Unlock Hisab")
                .setSubtitle("Confirm your fingerprint or face to access store calculations")
                .setNegativeButton("Use PIN", executor) { _, _ ->
                    activity.runOnUiThread {
                        onError("PIN requested")
                    }
                }
                .build()

            prompt.authenticate(
                cancellationSignal,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        activity.runOnUiThread {
                            onSuccess()
                        }
                    }

                    override fun onAuthenticationFailed() {
                        activity.runOnUiThread {
                            onError("Biometric not recognized. Try again or enter PIN.")
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        activity.runOnUiThread {
                            onError(errString?.toString() ?: "Biometric error")
                        }
                    }
                }
            )
        } else {
            onError("Biometrics not supported on this Android version")
        }
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((pin + SALT).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
