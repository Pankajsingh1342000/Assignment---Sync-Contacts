package com.example.synccontacts.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    val name: String?,
    val title: String?,
    val phone: String?,
    val email: String?
) : Parcelable 