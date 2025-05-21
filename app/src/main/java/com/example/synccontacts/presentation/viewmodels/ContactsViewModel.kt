package com.example.synccontacts.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.synccontacts.data.DeviceContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.provider.ContactsContract
import android.net.Uri

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val _allContacts = MutableLiveData<List<DeviceContact>>()
    private val _filteredContacts = MutableLiveData<List<DeviceContact>>()
    val contacts: LiveData<List<DeviceContact>> = _filteredContacts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val deviceContacts = withContext(Dispatchers.IO) {
                    queryDeviceContacts()
                }
                _allContacts.value = deviceContacts
                _filteredContacts.value = deviceContacts
            } catch (e: SecurityException) {
                 _error.value = "Permission denied to read contacts. Please grant the permission in Settings."
                 _allContacts.value = emptyList()
                 _filteredContacts.value = emptyList()
            } catch (e: Exception) {
                _error.value = "Failed to load contacts: ${e.message}"
                _allContacts.value = emptyList()
                _filteredContacts.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterContacts(query: String?) {
        val currentContacts = _allContacts.value ?: return
        if (query.isNullOrEmpty()) {
            _filteredContacts.value = currentContacts
        } else {
            val lowerCaseQuery = query.lowercase()
            _filteredContacts.value = currentContacts.filter {
                it.name?.lowercase()?.contains(lowerCaseQuery) == true ||
                it.phoneNumber?.lowercase()?.contains(lowerCaseQuery) == true
            }
        }
    }

    private fun queryDeviceContacts(): List<DeviceContact> {
        val contactsList = mutableListOf<DeviceContact>()
        val uri: Uri = ContactsContract.Contacts.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )
        val selection: String? = null
        val selectionArgs: Array<String>? = null
        val sortOrder: String = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"

        getApplication<Application>().contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val lookupKeyColumn = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
            val nameColumn = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val hasPhoneNumberColumn = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val lookupKey = cursor.getString(lookupKeyColumn)
                val name = cursor.getString(nameColumn)
                val hasPhoneNumber = cursor.getInt(hasPhoneNumberColumn) > 0

                var phoneNumber: String? = null
                if (hasPhoneNumber) {

                    val phoneCursor = getApplication<Application>().contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id.toString()),
                        null
                    )
                    phoneCursor?.use { pCur ->
                        if (pCur.moveToFirst()) {
                            phoneNumber = pCur.getString(pCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        }
                    }
                }

                val contactUri = ContactsContract.Contacts.getLookupUri(id, lookupKey)

                contactsList.add(DeviceContact(id, lookupKey, name, phoneNumber, null, contactUri))
            }
        }
        return contactsList
    }
} 