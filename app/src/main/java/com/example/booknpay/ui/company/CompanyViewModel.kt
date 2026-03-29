package com.example.booknpay.ui.company

import androidx.lifecycle.ViewModel
import com.example.booknpay.data.model.Company
import com.example.booknpay.data.model.CompanyService
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.jvm.java

@HiltViewModel
class CompanyViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _companies = MutableStateFlow<List<Company>>(emptyList())
    val companies = _companies.asStateFlow()

    init {
        fetchCompanies()
    }

    private fun fetchCompanies() {
        // Inside your CompanyViewModel (fetching logic)
        firestore.collection("companies")
            .addSnapshotListener { snapshot, _ ->
                val companyList = snapshot?.documents?.mapNotNull { doc ->
                    // 1. Map the document to the class
                    val company = doc.toObject(Company::class.java)
                    // 2. Explicitly attach the document ID to the 'id' field
                    company?.copy(id = doc.id)
                } ?: emptyList()
                _companies.value = companyList
            }
    }

    // Fetch routes for a specific company (e.g., Sososo or AXA)
    fun getCompanyServices(companyId: String): Flow<List<CompanyService>> = callbackFlow {
        val listener = firestore.collection("companies")
            .document(companyId)
            .collection("services")
            .addSnapshotListener { snapshot, _ ->
                val services = snapshot?.toObjects(CompanyService::class.java) ?: emptyList()
                trySend(services)
            }
        awaitClose { listener.remove() }
    }
}