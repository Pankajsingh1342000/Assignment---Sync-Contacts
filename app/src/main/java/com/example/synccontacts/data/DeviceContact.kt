package com.example.synccontacts.data

import android.net.Uri

data class DeviceContact(
    val id: Long,
    val lookupKey: String?,
    val name: String?,
    val phoneNumber: String?,
    val photoUri: Uri?,
    val lookupUri: Uri?
) 