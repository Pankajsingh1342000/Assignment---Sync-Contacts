package com.example.synccontacts.presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.synccontacts.data.DeviceContact
import com.example.synccontacts.R

class ContactAdapter(
    private val onContactClick: (DeviceContact) -> Unit
) : ListAdapter<DeviceContact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact, onContactClick)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.text_contact_name)
        private val phoneTextView: TextView = itemView.findViewById(R.id.text_contact_phone)

        fun bind(deviceContact: DeviceContact, onContactClick: (DeviceContact) -> Unit) {
            nameTextView.text = deviceContact.name ?: "No Name"
            phoneTextView.text = deviceContact.phoneNumber ?: "No Phone"

            itemView.setOnClickListener { onContactClick(deviceContact) }
        }
    }

    private class ContactDiffCallback : DiffUtil.ItemCallback<DeviceContact>() {
        override fun areItemsTheSame(oldItem: DeviceContact, newItem: DeviceContact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DeviceContact, newItem: DeviceContact): Boolean {
            return oldItem == newItem
        }
    }
} 