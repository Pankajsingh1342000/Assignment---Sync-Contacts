package com.example.synccontacts.data

import retrofit2.Response
import retrofit2.http.GET

interface ContactApiService {

    @GET("api/contacts")
    suspend fun getContacts(): Response<ApiResponse>
}

data class ApiResponse(
    val success: Boolean,
    val Data: ContactData
)

data class ContactData(
    val date: String,
    val totalUsers: Int,
    val users: List<ApiUser>
)

data class ApiUser(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val course: String,
    val enrolledOn: String
) 