package com.example.booknpay.util


import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

object GoogleSignInHelper {

    // Replace with your actual Web Client ID from Firebase Console
    private const val WEB_CLIENT_ID = "1:936031509967:web:afbe5e0491b886ca0446ba"

    suspend fun signInWithGoogle(context: Context): Result<String> {
        return try {
            val credentialManager = CredentialManager.create(context)

            // Generate nonce for security
            val nonce = UUID.randomUUID().toString()
            val hashedNonce = hashNonce(nonce)

            // Configure Google ID option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setNonce(hashedNonce)
                .build()

            // Build credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Get credential
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            // Parse Google ID Token
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val idToken = credential.idToken

            Result.success(idToken)

        } catch (e: GetCredentialException) {
            Result.failure(Exception("Google Sign-In failed: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    suspend fun signInWithGoogleAndFirebase(context: Context): Result<String> {
        return try {
            val tokenResult = signInWithGoogle(context)

            if (tokenResult.isFailure) {
                return Result.failure(tokenResult.exceptionOrNull() ?: Exception("Unknown error"))
            }

            val idToken = tokenResult.getOrThrow()

            // Sign in to Firebase
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = FirebaseAuth.getInstance()
                .signInWithCredential(firebaseCredential)
                .await()

            Result.success(authResult.user?.uid ?: "")

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hashNonce(nonce: String): String {
        val bytes = nonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}