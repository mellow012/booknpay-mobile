package com.example.booknpay.data.repository

import com.example.booknpay.data.model.AuthResult
import com.example.booknpay.data.model.RegisterRequest
import com.example.booknpay.data.model.User
import com.example.booknpay.domain.repository.AuthRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImp @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val usersCollection = firestore.collection("users")

    override fun getCurrentUser(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun getUserProfile(userId: String): AuthResult<User> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("User profile not found")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to fetch user profile")
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return AuthResult.Error("User ID not found")

            // Correctly return the result of getUserProfile
            return getUserProfile(userId)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun signUpWithEmail(request: RegisterRequest): AuthResult<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(
                request.email,
                request.password
            ).await()

            val userId = authResult.user?.uid ?: return AuthResult.Error("Failed to create account")

            val user = User(
                id = userId,
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                phone = request.phone,
                role = "customer",
                passwordSet = true,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            usersCollection.document(userId).set(user).await()

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: return AuthResult.Error("Google sign-in failed")

            val userId = firebaseUser.uid

            val existingUser = usersCollection.document(userId).get().await()

            if (existingUser.exists()) {
                val user = existingUser.toObject(User::class.java)!!
                AuthResult.Success(user)
            } else {
                val displayName = firebaseUser.displayName ?: ""
                val nameParts = displayName.split(" ")
                val firstName = nameParts.firstOrNull() ?: ""
                val lastName = nameParts.drop(1).joinToString(" ")

                val user = User(
                    id = userId,
                    email = firebaseUser.email ?: "",
                    firstName = firstName,
                    lastName = lastName,
                    phone = firebaseUser.phoneNumber ?: "",
                    role = "customer",
                    passwordSet = false,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )

                usersCollection.document(userId).set(user).await()
                AuthResult.Success(user)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed")
        }
    }

    override suspend fun signOut(): AuthResult<Unit> {
        return try {
            firebaseAuth.signOut()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign out failed")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send reset email")
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}
