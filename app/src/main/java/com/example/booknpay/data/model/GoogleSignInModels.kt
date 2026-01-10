
package com.example.booknpay.data.model

data class SignInWithGoogleRequest(
    val clientId: String
)

data class SignInWithGoogleResult(
    val idToken: String,
    val user: User? = null
)
