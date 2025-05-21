package com.example.synccontacts.presentation.ui

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.synccontacts.R
import com.google.android.material.textfield.TextInputEditText

class AddContactFragment : Fragment() {

    private lateinit var editTextFirstName: TextInputEditText
    private lateinit var editTextSurname: TextInputEditText
    private lateinit var editTextCompany: TextInputEditText
    private lateinit var editTextPhone: TextInputEditText
    private lateinit var buttonSaveContact: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextFirstName = view.findViewById(R.id.edit_text_first_name)
        editTextSurname = view.findViewById(R.id.edit_text_surname)
        editTextCompany = view.findViewById(R.id.edit_text_company)
        editTextPhone = view.findViewById(R.id.edit_text_phone)
        buttonSaveContact = view.findViewById(R.id.button_save_contact)

        buttonSaveContact.setOnClickListener {
            saveContact()
        }
    }

    private fun saveContact() {
        val firstName = editTextFirstName.text.toString()
        val surname = editTextSurname.text.toString()
        val company = editTextCompany.text.toString()
        val phone = editTextPhone.text.toString()

        if (firstName.isEmpty() && surname.isEmpty() && company.isEmpty() && phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter contact details", Toast.LENGTH_SHORT).show()
            return
        }

        val contentValues = ContentValues().apply {
            put(ContactsContract.RawContacts.ACCOUNT_TYPE, null as String?)
            put(ContactsContract.RawContacts.ACCOUNT_NAME, null as String?)
        }

        val rawContactUri: Uri? = requireContext().contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues)
        val rawContactId = rawContactUri?.lastPathSegment?.toLong()

        if (rawContactId != null) {

            if (firstName.isNotEmpty() || surname.isNotEmpty()) {
                val nameValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                    put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, surname)
                }
                requireContext().contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)
            }

            if (phone.isNotEmpty()) {
                val phoneValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                }
                requireContext().contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneValues)
            }

            if (company.isNotEmpty()) {
                val companyValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                }
                requireContext().contentResolver.insert(ContactsContract.Data.CONTENT_URI, companyValues)
            }

            Toast.makeText(requireContext(), "Contact saved", Toast.LENGTH_SHORT).show()
            findNavController().previousBackStackEntry?.savedStateHandle?.set("forceRefresh", true)
            findNavController().popBackStack()

        } else {
            Toast.makeText(requireContext(), "Failed to save contact", Toast.LENGTH_SHORT).show()
        }
    }
}