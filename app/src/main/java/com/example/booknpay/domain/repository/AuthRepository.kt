package com.example.booknpay.domain.repository

import com.example.booknpay.data.model.AuthResult
import com.example.booknpay.data.model.RegisterRequest
import com.example.booknpay.data.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<FirebaseUser?>
    suspend fun getUserProfile(userId: String): AuthResult<User>
    suspend fun signInWithEmail(email: String, password: String): AuthResult<User>
    suspend fun signUpWithEmail(request: RegisterRequest): AuthResult<User>
    suspend fun signInWithGoogle(idToken: String): AuthResult<User>
    suspend fun signOut(): AuthResult<Unit>
    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
}
