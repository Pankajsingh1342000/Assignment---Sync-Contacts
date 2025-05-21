package com.example.synccontacts.presentation.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.synccontacts.R
import com.example.synccontacts.presentation.viewmodels.ContactsViewModel

class ContactsFragment : Fragment(), SearchView.OnQueryTextListener {

    private val viewModel: ContactsViewModel by viewModels()

    private lateinit var contactAdapter: ContactAdapter
    private lateinit var recyclerViewContacts: RecyclerView
    private lateinit var progressBarContacts: ProgressBar
    private lateinit var fabAddContact: FloatingActionButton
    private lateinit var fabSyncContacts: FloatingActionButton
    private lateinit var searchView: SearchView

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
        searchView = view.findViewById(R.id.search_view)

        setupRecyclerView()
        observeViewModel()
        setupSearchView()

        fabAddContact.setOnClickListener {
            findNavController().navigate(R.id.action_contactsFragment_to_addContactFragment)
        }

        fabSyncContacts.setOnClickListener {
            findNavController().navigate(R.id.action_contactsFragment_to_newContactsFoundFragment)
        }
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter {
            if (it.lookupUri != null) {
                val action = ContactsFragmentDirections.actionContactsFragmentToEditContactFragment(it.lookupUri.toString())
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

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.filterContacts(newText)
        return true
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadContacts()
    }
} 