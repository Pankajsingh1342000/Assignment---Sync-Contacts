package com.example.synccontacts.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.synccontacts.data.ContactRepository

class NewContactsFoundViewModelFactory(
    private val repository: ContactRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewContactsFoundViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewContactsFoundViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}