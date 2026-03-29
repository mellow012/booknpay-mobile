package com.example.booknpay.data.repository

import com.example.booknpay.data.model.Company
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCompanyRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getCompanies(): List<Company> {
        return try {
            firestore.collection("companies")
                .whereEqualTo("status", "active")
                .get()
                .await()
                .toObjects(Company::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}