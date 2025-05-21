package com.example.synccontacts.presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.synccontacts.data.Contact
import com.example.synccontacts.R

class NewContactAdapter(
    private val onEditClick: (Contact) -> Unit
) : ListAdapter<Contact, NewContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_contact_new_found, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact, onEditClick)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.text_contact_name)
        private val titleTextView: TextView = itemView.findViewById(R.id.text_contact_title)
        private val phoneTextView: TextView = itemView.findViewById(R.id.text_contact_phone)
        private val emailTextView: TextView = itemView.findViewById(R.id.text_contact_email)
        private val editIcon: ImageView = itemView.findViewById(R.id.image_edit_contact)

        fun bind(contact: Contact, onEditClick: (Contact) -> Unit) {
            nameTextView.text = contact.name ?: "N/A"
            titleTextView.text = contact.title ?: "N/A"
            phoneTextView.text = contact.phone ?: "N/A"
            emailTextView.text = contact.email ?: "N/A"

            editIcon.setOnClickListener {
                val action = NewContactsFoundFragmentDirections.actionNewContactsFoundFragmentToEditContactFragment(newContact = contact)
                itemView.findNavController().navigate(action)
            }
        }
    }

    private class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            // Assuming phone number is a unique identifier for simplicity
            return oldItem.phone == newItem.phone
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
} 