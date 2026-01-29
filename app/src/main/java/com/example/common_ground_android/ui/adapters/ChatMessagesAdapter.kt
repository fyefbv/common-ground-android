package com.example.common_ground_android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.common_ground_android.R
import com.example.common_ground_android.data.models.ChatMessage
import com.example.common_ground_android.databinding.ItemMessageBinding

class ChatMessagesAdapter : ListAdapter<ChatMessage, ChatMessagesAdapter.MessageViewHolder>(
    MessageDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.messageText.text = message.text
            binding.messageTime.text = message.time

            if (message.fromMe) {
                binding.messageBubble.setBackgroundResource(R.drawable.bubble_outgoing)
                binding.messageText.setTextColor(binding.root.context.getColor(R.color.md_on_primary_container))
                binding.messageContainer.gravity = android.view.Gravity.END
                binding.userName.visibility = View.GONE

                binding.messageBubble.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    binding.root.context.getColor(R.color.md_primary_container)
                )
            } else {
                binding.messageBubble.setBackgroundResource(R.drawable.bubble_incoming)
                binding.messageText.setTextColor(binding.root.context.getColor(R.color.md_on_secondary_container))
                binding.messageContainer.gravity = android.view.Gravity.START

                if (message.userName.isNotEmpty()) {
                    binding.userName.visibility = View.VISIBLE
                    binding.userName.text = message.userName
                } else {
                    binding.userName.visibility = View.GONE
                }

                binding.messageBubble.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    binding.root.context.getColor(R.color.md_secondary_container)
                )
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.text == newItem.text && oldItem.time == newItem.time
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}