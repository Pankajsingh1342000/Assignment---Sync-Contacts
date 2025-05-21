package com.example.synccontacts.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synccontacts.data.Contact
import com.example.synccontacts.data.ContactRepository
import kotlinx.coroutines.launch

class NewContactsFoundViewModel(private val repository: ContactRepository) : ViewModel() {

    private val _newContacts = MutableLiveData<List<Contact>>()
    val newContacts: LiveData<List<Contact>> = _newContacts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchNewContacts()
    }

    private fun fetchNewContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val contacts = repository.getNewContacts()
                _newContacts.value = contacts
            } catch (e: Exception) {
                _error.value = e.message
                _newContacts.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
} 