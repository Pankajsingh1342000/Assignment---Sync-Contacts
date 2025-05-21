package com.example.synccontacts.presentation.ui

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synccontacts.R
import com.example.synccontacts.data.Contact
import com.example.synccontacts.data.NetworkModule
import com.example.synccontacts.data.ContactRepository
import com.example.synccontacts.presentation.viewmodels.NewContactsFoundViewModel
import com.example.synccontacts.presentation.viewmodels.NewContactsFoundViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewContactsFoundFragment : Fragment() {

    private val viewModel: NewContactsFoundViewModel by viewModels {
        NewContactsFoundViewModelFactory(ContactRepository(NetworkModule.contactApiService))
    }

    private lateinit var newContactAdapter: NewContactAdapter
    private lateinit var recyclerViewNewContacts: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonSyncAllContacts: TextView

    private val EDITED_NEW_CONTACT_KEY = "editedNewContact"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_contacts_found, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewNewContacts = view.findViewById(R.id.recycler_view_new_contacts)
        progressBar = view.findViewById(R.id.progress_bar)
        buttonSyncAllContacts = view.findViewById(R.id.button_sync_all_contacts)

        setupRecyclerView()
        observeViewModel()
        observeEditedContactResult()

        buttonSyncAllContacts.setOnClickListener {
            syncAllContacts()
        }
    }

    private fun setupRecyclerView() {
        newContactAdapter = NewContactAdapter()
        recyclerViewNewContacts.layoutManager = LinearLayoutManager(context)
        recyclerViewNewContacts.adapter = newContactAdapter
    }

    private fun observeViewModel() {
        viewModel.newContacts.observe(viewLifecycleOwner) {
            newContactAdapter.submitList(it)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) {
            if (it != null) {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeEditedContactResult() {
        val navController = findNavController()
        val currentBackStackEntry = navController.currentBackStackEntry
        val savedStateHandle = currentBackStackEntry?.savedStateHandle

        savedStateHandle?.getLiveData<Contact>(EDITED_NEW_CONTACT_KEY)?.observe(viewLifecycleOwner) { editedContact ->
            if (editedContact != null) {
                val currentList = viewModel.newContacts.value?.toMutableList() ?: mutableListOf()
                val index =
                    currentList.indexOfFirst { it.phone == editedContact.phone } // Assuming phone is unique
                if (index != -1) {
                    currentList[index] = editedContact

                    viewModel.updateNewContact(editedContact)

                    savedStateHandle.remove<Contact>(EDITED_NEW_CONTACT_KEY)
                }
            }
        }
    }

    private fun syncAllContacts() {
        val contactsToSync = viewModel.newContacts.value ?: return
        buttonSyncAllContacts.isEnabled = false
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            var syncedCount = 0
            var failedCount = 0

            for (contact in contactsToSync) {
                try {
                    if (contact.phone != null && contactExists(contact.phone)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Contact ${contact.name} already exists", Toast.LENGTH_SHORT).show()
                        }
                        continue
                    }

                    val contentValues = ContentValues().apply {
                        put(ContactsContract.RawContacts.ACCOUNT_TYPE, null as String?)
                        put(ContactsContract.RawContacts.ACCOUNT_NAME, null as String?)
                    }

                    val rawContactUri: Uri? = requireContext().contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues)
                    val rawContactId = rawContactUri?.lastPathSegment?.toLong()

                    if (rawContactId != null) {

                        if (!contact.name.isNullOrEmpty()) {
                            val nameValues = ContentValues().apply {
                                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

                                val names = contact.name.trim().split(" ", limit = 2)
                                put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, names.firstOrNull() ?: contact.name)
                                put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, names.getOrNull(1))
                            }
                            requireContext().contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)
                        }

                        if (!contact.phone.isNullOrEmpty()) {
                            val phoneValues = ContentValues().apply {
                                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phone)
                                put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            }
                            requireContext().contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneValues)
                        }

                        if (!contact.email.isNullOrEmpty()) {
                            val emailValues = ContentValues().apply {
                                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                                put(ContactsContract.CommonDataKinds.Email.ADDRESS, contact.email)
                                put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                            }
                            requireContext().contentResolver.insert(ContactsContract.Data.CONTENT_URI, emailValues)
                        }

                        if (!contact.title.isNullOrEmpty()) {
                            val orgValues = ContentValues().apply {
                                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                                put(ContactsContract.CommonDataKinds.Organization.TITLE, contact.title)
                            }
                            requireContext().contentResolver.insert(ContactsContract.Data.CONTENT_URI, orgValues)
                        }

                        syncedCount++
                    }
                } catch (e: Exception) {
                    failedCount++
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to sync contact ${contact.name}: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                buttonSyncAllContacts.isEnabled = true
                val message = if (failedCount > 0) {
                    "Synced $syncedCount contacts, failed to sync $failedCount contacts"
                } else {
                    "Successfully synced $syncedCount contacts"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun contactExists(phoneNumber: String): Boolean {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup._ID)
        requireContext().contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            return cursor.moveToFirst()
        }
        return false
    }
} 