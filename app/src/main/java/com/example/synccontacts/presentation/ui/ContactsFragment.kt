package com.example.synccontacts.presentation.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.synccontacts.R
import com.example.synccontacts.presentation.viewmodels.ContactsViewModel
import android.text.TextWatcher
import android.text.Editable

class ContactsFragment : Fragment() {

    private val viewModel: ContactsViewModel by viewModels()

    private lateinit var contactAdapter: ContactAdapter
    private lateinit var recyclerViewContacts: RecyclerView
    private lateinit var progressBarContacts: ProgressBar
    private lateinit var fabAddContact: FloatingActionButton
    private lateinit var fabSyncContacts: FloatingActionButton
    private lateinit var editTextSearch: EditText
    private lateinit var imageMicIcon: ImageView
    private lateinit var imageThreeDotsIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewContacts = view.findViewById(R.id.recycler_view_contacts)
        progressBarContacts = view.findViewById(R.id.progress_bar_contacts)
        fabAddContact = view.findViewById(R.id.fab_add_contact)
        fabSyncContacts = view.findViewById(R.id.fab_sync_contacts)

        val customSearchBar = view.findViewById<View>(R.id.custom_search_bar)
        editTextSearch = customSearchBar.findViewById(R.id.edit_text_search)
        imageMicIcon = customSearchBar.findViewById(R.id.image_mic_icon)
        imageThreeDotsIcon = customSearchBar.findViewById(R.id.image_three_dots_icon)

        setupRecyclerView()
        observeViewModel()
        setupSearch()
        setupFabListeners()
        observeSavedState()
    }

    private fun observeSavedState() {
        val navController = findNavController()
        val currentBackStackEntry = navController.currentBackStackEntry
        val savedStateHandle = currentBackStackEntry?.savedStateHandle

        savedStateHandle?.getLiveData<Boolean>("forceRefresh")?.observe(viewLifecycleOwner) { forceRefresh ->
            if (forceRefresh) {
                viewModel.loadContacts(true)
                savedStateHandle.remove<Boolean>("forceRefresh")
            }
        }
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter {
            if (it.lookupUri != null) {
                val action = ContactsFragmentDirections.actionContactsFragmentToEditContactFragment(contactUri = it.lookupUri.toString())
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Cannot edit this contact", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerViewContacts.layoutManager = LinearLayoutManager(context)
        recyclerViewContacts.adapter = contactAdapter
    }

    private fun observeViewModel() {
        viewModel.contacts.observe(viewLifecycleOwner) {
            contactAdapter.submitList(it)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            progressBarContacts.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) {
            if (it != null) {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSearch() {
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterContacts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFabListeners() {
         fabAddContact.setOnClickListener {
            findNavController().navigate(R.id.action_contactsFragment_to_addContactFragment)
        }

        fabSyncContacts.setOnClickListener {
            findNavController().navigate(R.id.action_contactsFragment_to_newContactsFoundFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadContacts(false)
    }
} 