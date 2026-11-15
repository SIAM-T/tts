package com.example.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.hardware.biometrics.BiometricManager
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
        private const val KEY_AUTO_LOCK = "key_auto_lock"
        private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled"
        private const val KEY_CURRENCY = "key_currency"
        private const val KEY_STORE_NAME = "key_store_name"
        private const val KEY_TARGET_PROFIT = "key_target_profit"
        private const val SALT = "hisab_store_salt_2026_secure"
    }

    fun isPinEnabled(): Boolean {
        return prefs.getBoolean(KEY_PIN_ENABLED, false) && prefs.getString(KEY_PIN_HASH, null) != null
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false) && isPinEnabled()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).commit()
    }

    fun isAutoLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_LOCK, true)
    }

    fun setAutoLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LOCK, enabled).commit()
    }

    fun isHapticsEnabled(): Boolean {
        return prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).commit()
    }

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_PIN_ENABLED, true)
            .commit()
    }

    fun removePin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .commit()
    }

    fun verifyPin(inputPin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return true
        return hashPin(inputPin) == storedHash
    }

    fun getCurrency(): String {
        return prefs.getString(KEY_CURRENCY, "$")?.ifBlank { "$" } ?: "$"
    }

    fun setCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency.ifBlank { "$" }).commit()
    }

    fun getStoreName(): String {
        val raw = prefs.getString(KEY_STORE_NAME, "My Store") ?: "My Store"
        return raw.ifBlank { "My Store" }
    }

    fun setStoreName(name: String) {
        val cleaned = name.trim().ifBlank { "My Store" }
        prefs.edit().putString(KEY_STORE_NAME, cleaned).commit()
    }

    fun getDailyTargetProfit(): Double {
        return prefs.getFloat(KEY_TARGET_PROFIT, 100.0f).toDouble().coerceAtLeast(1.0)
    }

    fun setDailyTargetProfit(target: Double) {
        prefs.edit().putFloat(KEY_TARGET_PROFIT, target.coerceAtLeast(1.0).toFloat()).commit()
    }

    fun canUseBiometrics(): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bm = context.getSystemService(BiometricManager::class.java)
                val status = bm?.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                return status == BiometricManager.BIOMETRIC_SUCCESS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val bm = context.getSystemService(BiometricManager::class.java)
                @Suppress("DEPRECATION")
                return bm?.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
            }
        } catch (_: Throwable) { }

        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return km?.isDeviceSecure == true
    }

    fun getBiometricStatusMessage(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bm = context.getSystemService(BiometricManager::class.java)
                when (bm?.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
                )) {
                    BiometricManager.BIOMETRIC_SUCCESS -> "Fingerprint & Face unlock ready"
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No fingerprint/face enrolled in device Settings"
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric sensor detected on this device"
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric sensor temporarily unavailable"
                    else -> "Device biometric unavailable"
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (canUseBiometrics()) "Biometric unlock ready" else "Set up fingerprint in Android Settings"
            } else {
                "Requires Android 9.0 or higher"
            }
        } catch (_: Throwable) {
            "Biometric check unavailable"
        }
    }

    fun triggerBiometricPrompt(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ): CancellationSignal? {
        if (activity.isFinishing || activity.isDestroyed) return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!canUseBiometrics()) {
                onError(getBiometricStatusMessage())
                return null
            }
            return try {
                val executor = Executors.newSingleThreadExecutor()
                val cancellationSignal = CancellationSignal()

                val builder = BiometricPrompt.Builder(activity)
                    .setTitle("Unlock Hisab")
                    .setSubtitle("Verify your fingerprint or face")
                    .setDescription("Keep your store revenue and profit records private")
                    .setNegativeButton("Use PIN", executor) { _, _ ->
                        activity.runOnUiThread {
                            onError("Enter your 4-digit PIN")
                        }
                    }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    builder.setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.BIOMETRIC_WEAK
                    )
                }

                builder.build().authenticate(
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
                                val msg = errString?.toString() ?: "Biometric cancelled"
                                onError(msg)
                            }
                        }
                    }
                )
                cancellationSignal
            } catch (e: Exception) {
                onError("Biometric unavailable: ${e.localizedMessage ?: "Error"}")
                null
            }
        } else {
            onError("Biometrics require Android 9.0 or higher")
            return null
        }
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((pin + SALT).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
