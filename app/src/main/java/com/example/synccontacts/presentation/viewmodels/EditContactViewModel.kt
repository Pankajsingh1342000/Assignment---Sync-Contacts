package com.example.synccontacts.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.synccontacts.data.Contact

class EditContactViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    // Key to use for the edited contact in SavedStateHandle
    private val EDITED_NEW_CONTACT_KEY = "editedNewContact"

    fun setEditedNewContact(contact: Contact) {
        savedStateHandle[EDITED_NEW_CONTACT_KEY] = contact
    }

    // Function to retrieve the edited contact from the previous fragment (NewContactsFoundFragment)
    fun getEditedNewContact(): Contact? {
        // This function is not directly used in EditContactViewModel,
        // but the key is defined here for consistency.
        return savedStateHandle[EDITED_NEW_CONTACT_KEY]
    }
} 