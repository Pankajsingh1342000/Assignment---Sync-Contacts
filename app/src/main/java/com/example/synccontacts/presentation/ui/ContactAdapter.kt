package com.example.synccontacts.presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.synccontacts.data.DeviceContact
import com.example.synccontacts.R

class ContactAdapter(
    private val onContactClick: (DeviceContact) -> Unit
) : ListAdapter<DeviceContact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    private val colors = listOf(
        R.color.initial_bg_blue,
        R.color.initial_bg_purple,
        R.color.initial_bg_green,
        R.color.initial_bg_red
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact, onContactClick, colors[position % colors.size])
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val initialTextView: TextView = itemView.findViewById(R.id.text_contact_initial)
        private val nameTextView: TextView = itemView.findViewById(R.id.text_contact_name)

        fun bind(deviceContact: DeviceContact, onContactClick: (DeviceContact) -> Unit, bgColor: Int) {
            val initial = deviceContact.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            initialTextView.text = initial
            initialTextView.background.setColorFilter(ContextCompat.getColor(itemView.context, bgColor), android.graphics.PorterDuff.Mode.SRC_IN)

            nameTextView.text = deviceContact.name ?: "No Name"

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