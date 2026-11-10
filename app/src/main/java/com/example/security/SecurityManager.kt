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
        private const val KEY_STORE_NAME = "key_store_name"
        private const val KEY_TARGET_PROFIT = "key_target_profit"
        private const val SALT = "hisab_store_salt_2026_secure"
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

    fun getStoreName(): String {
        return prefs.getString(KEY_STORE_NAME, "My Store") ?: "My Store"
    }

    fun setStoreName(name: String) {
        prefs.edit().putString(KEY_STORE_NAME, name.trim()).apply()
    }

    fun getDailyTargetProfit(): Double {
        return prefs.getFloat(KEY_TARGET_PROFIT, 100.0f).toDouble()
    }

    fun setDailyTargetProfit(target: Double) {
        prefs.edit().putFloat(KEY_TARGET_PROFIT, target.toFloat()).apply()
    }

    /**
     * Checks if biometric hardware is present or enrolled on device.
     */
    fun canUseBiometrics(): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bm = context.getSystemService(android.hardware.biometrics.BiometricManager::class.java)
                val status = bm?.canAuthenticate(
                    android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                if (status == android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS) {
                    return true
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val bm = context.getSystemService(android.hardware.biometrics.BiometricManager::class.java)
                @Suppress("DEPRECATION")
                if (bm?.canAuthenticate() == android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS) {
                    return true
                }
            }
        } catch (_: Throwable) { }

        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return km?.isDeviceSecure == true
    }

    /**
     * Triggers the biometric prompt safely with robust error handling and fallback.
     */
    fun triggerBiometricPrompt(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val executor = Executors.newSingleThreadExecutor()
                val cancellationSignal = CancellationSignal()

                val prompt = BiometricPrompt.Builder(activity)
                    .setTitle("Unlock Hisab")
                    .setSubtitle("Confirm your fingerprint or face to access store records")
                    .setDescription("Keep your store revenue and financial records secure")
                    .setNegativeButton("Use 4-Digit PIN", executor) { _, _ ->
                        activity.runOnUiThread {
                            onError("Please enter your PIN")
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
                                onError("Biometric not recognized. Try again or use PIN.")
                            }
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                            activity.runOnUiThread {
                                val msg = errString?.toString() ?: "Biometric authentication cancelled"
                                onError(msg)
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                onError("Biometric authentication unavailable: ${e.localizedMessage ?: "Unknown error"}")
            }
        } else {
            onError("Biometrics require Android 9.0 or higher")
        }
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((pin + SALT).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
