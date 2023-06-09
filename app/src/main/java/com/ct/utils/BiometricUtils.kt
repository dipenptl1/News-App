package com.ct.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ct.R

/**
* Util file to handle biometric operations
* */
object BiometricUtils {

    interface BiometricAuthListener {
        // Executes when device does not support biometric
        fun onBiometricNotSupported()
        // Executes when fingerprint Authentication is succeed
        fun onBiometricAuthenticationSuccess(result: BiometricPrompt.AuthenticationResult)
        // Executes when any error occurred while fingerprint Authentication
        fun onBiometricAuthenticationError(errorCode: Int, errorMessage: String)
    }

    private fun isBiometricSupported(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate =
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
    * Launch this method when fingerprint verification is required
    * This method will execute following callback [BiometricUtils.BiometricAuthListener]
    * */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        listener: BiometricAuthListener,
        title: String = activity.getString(R.string.biometric_authentication),
        subtitle: String = activity.getString(R.string.enter_biometric_credentials),
        description: String = activity.getString(R.string.input_your_fingerprint),
    ) {
        if (isBiometricSupported(activity)) {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText(activity.getString(R.string.cancel))
                .build()

            val biometricPrompt = getBiometricPrompt(activity, listener)

            Handler(Looper.getMainLooper()).post {
                biometricPrompt.authenticate(promptInfo)
            }
        } else {
            listener.onBiometricNotSupported()
        }
    }

    private fun getBiometricPrompt(
        activity: FragmentActivity,
        listener: BiometricAuthListener
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.onBiometricAuthenticationError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                listener.onBiometricAuthenticationError(
                    100,
                    activity.getString(R.string.authentication_failed)
                )
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener.onBiometricAuthenticationSuccess(result)
            }
        }

        return BiometricPrompt(activity, executor, callback)
    }
}