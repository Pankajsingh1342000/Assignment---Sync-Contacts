package com.example.synccontacts.data

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ContactRepository(private val contactApiService: ContactApiService) {

    suspend fun getNewContacts(): List<Contact> {
        return try {
            val response = contactApiService.getContacts()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    // Map API users to our Contact model
                    apiResponse.Data.users.map { user ->
                        Contact(
                            name = user.fullName,
                            title = user.course,
                            phone = user.phone,
                            email = user.email
                        )
                    }
                } else {
                    emptyList()
                }
            } else {
                throw Exception("Server error: ${response.code()} - ${response.message()}")
            }
        } catch (e: SocketTimeoutException) {
            throw Exception("Connection timed out. Please check your internet connection and try again.")
        } catch (e: UnknownHostException) {
            throw Exception("Unable to reach the server. Please check your internet connection.")
        } catch (e: IOException) {
            throw Exception("Network error: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Failed to fetch contacts: ${e.message}")
        }
    }
} 