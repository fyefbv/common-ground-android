package com.example.common_ground_android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.ItemRouletteMessageBinding
import com.example.common_ground_android.network.model.domain.ChatRouletteMessage
import com.example.common_ground_android.utils.DateUtils

class ChatRouletteMessageAdapter(
    private val currentProfileId: String
) : ListAdapter<ChatRouletteMessage, ChatRouletteMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemRouletteMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(private val binding: ItemRouletteMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatRouletteMessage) {
            val isOwn = message.senderId == currentProfileId
            binding.messageText.text = message.content
            binding.messageTime.text = DateUtils.formatToTime(message.createdAt)

            if (isOwn) {
                binding.root.gravity = android.view.Gravity.END
                binding.messageBubble.setCardBackgroundColor(
                    binding.root.context.getColor(R.color.md_primary_container)
                )
                binding.messageText.setTextColor(
                    binding.root.context.getColor(R.color.md_on_primary_container)
                )
                binding.messageTime.setTextColor(
                    binding.root.context.getColor(R.color.md_on_primary_container)
                )
            } else {
                binding.root.gravity = android.view.Gravity.START
                binding.messageBubble.setCardBackgroundColor(
                    binding.root.context.getColor(R.color.md_surface_container_high)
                )
                binding.messageText.setTextColor(
                    binding.root.context.getColor(R.color.md_on_surface)
                )
                binding.messageTime.setTextColor(
                    binding.root.context.getColor(R.color.md_on_surface_variant)
                )
            }
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<ChatRouletteMessage>() {
        override fun areItemsTheSame(oldItem: ChatRouletteMessage, newItem: ChatRouletteMessage): Boolean =
            oldItem.createdAt == newItem.createdAt && oldItem.senderId == newItem.senderId
        override fun areContentsTheSame(oldItem: ChatRouletteMessage, newItem: ChatRouletteMessage): Boolean =
            oldItem == newItem
    }
}