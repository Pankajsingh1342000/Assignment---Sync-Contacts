package com.example.synccontacts.presentation.ui

import android.content.ContentProviderOperation
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.example.synccontacts.R
import com.example.synccontacts.data.Contact
import androidx.core.net.toUri

class EditContactFragment : Fragment() {

    private val args: EditContactFragmentArgs by navArgs()

    private lateinit var editTextFirstName: TextInputEditText
    private lateinit var editTextSurname: TextInputEditText
    private lateinit var editTextCompany: TextInputEditText
    private lateinit var editTextPhone: TextInputEditText
    private lateinit var buttonUpdateContact: Button

    private var contactId: Long? = null
    private var rawContactId: Long? = null
    private var isEditingNewContact: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextFirstName = view.findViewById(R.id.edit_text_edit_first_name)
        editTextSurname = view.findViewById(R.id.edit_text_edit_surname)
        editTextCompany = view.findViewById(R.id.edit_text_edit_company)
        editTextPhone = view.findViewById(R.id.edit_text_edit_phone)
        buttonUpdateContact = view.findViewById(R.id.button_update_contact)

        val newContact = args.newContact
        val contactUriString = args.contactUri

        if (newContact != null) {
            isEditingNewContact = true
            populateUiWithContact(newContact)
            contactId = null
            rawContactId = null
        } else if (!contactUriString.isNullOrEmpty()) {
            isEditingNewContact = false
            val contactUri = contactUriString.toUri()
            loadContactDetails(contactUri)
        } else {
            Toast.makeText(requireContext(), "Error loading contact", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        buttonUpdateContact.setOnClickListener {
            updateContact()
        }
    }

    private fun populateUiWithContact(contact: Contact) {
        editTextFirstName.setText(contact.name?.split(" ", limit = 2)?.getOrNull(0))
        editTextSurname.setText(contact.name?.split(" ", limit = 2)?.getOrNull(1))
        editTextCompany.setText(contact.title)
        editTextPhone.setText(contact.phone)
    }

    private fun loadContactDetails(contactUri: Uri) {
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )

        requireContext().contentResolver.query(
            contactUri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                contactId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val rawContactIdCursor = requireContext().contentResolver.query(
                    ContactsContract.RawContacts.CONTENT_URI,
                    arrayOf(ContactsContract.RawContacts._ID),
                    ContactsContract.RawContacts.CONTACT_ID + " = ?",
                    arrayOf(contactId.toString()),
                    null
                )
                rawContactIdCursor?.use { rawCursor ->
                    if (rawCursor.moveToFirst()) {
                        rawContactId = rawCursor.getLong(rawCursor.getColumnIndexOrThrow(ContactsContract.RawContacts._ID))
                    }
                }

                loadStructuredName(rawContactId)
                loadPhone(rawContactId)
                loadCompany(rawContactId)
            }
        }
    }

    private fun loadStructuredName(rawContactId: Long?) {
        if (rawContactId == null) return
        val cursor = requireContext().contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
            ),
            ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ?",
            arrayOf(
                rawContactId.toString(),
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            ),
            null
        )
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val givenName = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                val familyName = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                editTextFirstName.setText(givenName)
                editTextSurname.setText(familyName)
            }
        }
    }

    private fun loadPhone(rawContactId: Long?) {
        if (rawContactId == null) return
        val cursor = requireContext().contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ?" + " AND " +
                    ContactsContract.CommonDataKinds.Phone.TYPE + " = ?",
            arrayOf(
                rawContactId.toString(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE.toString()
            ),
            null
        )
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val phoneNumber = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                editTextPhone.setText(phoneNumber)
            }
        }
    }

    private fun loadCompany(rawContactId: Long?) {
        if (rawContactId == null) return
        val cursor = requireContext().contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Organization.COMPANY),
            ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ?",
            arrayOf(
                rawContactId.toString(),
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
            ),
            null
        )
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val company = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Organization.COMPANY))
                editTextCompany.setText(company)
            }
        }
    }

    private fun updateContact() {
        val firstName = editTextFirstName.text.toString()
        val surname = editTextSurname.text.toString()
        val company = editTextCompany.text.toString()
        val phone = editTextPhone.text.toString()

        if (firstName.isEmpty() && surname.isEmpty() && company.isEmpty() && phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter contact details", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditingNewContact) {

            val updatedContact = args.newContact?.copy(
                name = if (firstName.isNotEmpty() || surname.isNotEmpty()) "$firstName $surname".trim() else null,
                title = company,
                phone = phone,
                email = args.newContact?.email // Keep original email as it's not in edit layout
            )

            if (updatedContact != null) {

                val navController = findNavController()
                navController.previousBackStackEntry?.savedStateHandle?.set("editedNewContact", updatedContact)

            }

            findNavController().popBackStack()

        } else {

            if (rawContactId == null) {
                Toast.makeText(requireContext(), "Contact not loaded for update", Toast.LENGTH_SHORT).show()
                return
            }
            val operations = arrayListOf<ContentProviderOperation>()

            val nameWhere = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ?"
            val nameArgs = arrayOf(rawContactId.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

            operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(nameWhere, nameArgs)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, surname)
                .build())

            val phoneWhere = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ?" + " AND " +
                    ContactsContract.CommonDataKinds.Phone.TYPE + " = ?"
            val phoneArgs = arrayOf(rawContactId.toString(), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE.toString())

            operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(phoneWhere, phoneArgs)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .build())

            val companyWhere = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ?"
            val companyArgs = arrayOf(rawContactId.toString(), ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)

            operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(companyWhere, companyArgs)
                .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                .build())

            try {
                requireContext().contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
                Toast.makeText(requireContext(), "Contact updated", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to update contact: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}