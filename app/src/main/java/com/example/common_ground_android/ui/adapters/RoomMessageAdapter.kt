package com.example.common_ground_android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.ItemMessageHeaderBinding
import com.example.common_ground_android.databinding.ItemMessageOtherBinding
import com.example.common_ground_android.databinding.ItemMessageSelfBinding
import com.example.common_ground_android.databinding.ItemSystemMessageBinding
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.Message
import com.example.common_ground_android.utils.DateUtils
import java.util.Calendar
import java.util.Date

class RoomMessageAdapter(
    private val currentProfileId: String,
    private val recyclerView: RecyclerView,
    private val onMessageLongClick: (Message, View) -> Unit = { _, _ -> },
    private val getParentMessage: (String) -> Message? = { null },
    private val onReplyClick: (String) -> Unit
) : ListAdapter<DisplayItem, RecyclerView.ViewHolder>(DisplayItemDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_SELF = 1
        private const val VIEW_TYPE_OTHER = 2
        private const val VIEW_TYPE_SYSTEM = 3
    }

    private var profilesMap: Map<String, Profile> = emptyMap()

    fun updateProfiles(profiles: Map<String, Profile>) {
        profilesMap = profiles
        notifyItemRangeChanged(0, itemCount)
    }

    fun highlightMessage(messageId: String) {
        val position = currentList.indexOfFirst {
            it is DisplayItem.MessageItem && it.message.id == messageId
        }
        if (position == -1) return
        when (val holder = recyclerView.findViewHolderForAdapterPosition(position)) {
            is SelfMessageViewHolder -> holder.highlight()
            is OtherMessageViewHolder -> holder.highlight()
        }
    }

    fun submitMessages(messages: List<Message>, onComplete: (() -> Unit)? = null) {
        val displayItems = mutableListOf<DisplayItem>()
        val today = Calendar.getInstance().time
        val yesterday = getYesterday()
        var lastDate: Date? = null

        messages.forEach { message ->
            val messageDate = message.createdAt
            if (lastDate == null || !isSameDay(messageDate, lastDate)) {
                val headerText = when {
                    isSameDay(messageDate, today) -> "Сегодня"
                    isSameDay(messageDate, yesterday) -> "Вчера"
                    else -> DateUtils.formatDate(messageDate)
                }
                displayItems.add(DisplayItem.Header(messageDate, headerText))
                lastDate = messageDate
            }
            displayItems.add(DisplayItem.MessageItem(message))
        }
        submitList(displayItems) {
            onComplete?.invoke()
        }
    }

    private fun getYesterday(): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return cal.time
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is DisplayItem.Header -> VIEW_TYPE_HEADER
            is DisplayItem.MessageItem -> {
                val message = item.message
                when {
                    message.isSystem -> VIEW_TYPE_SYSTEM
                    message.senderId == currentProfileId -> VIEW_TYPE_SELF
                    else -> VIEW_TYPE_OTHER
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemMessageHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_SELF -> {
                val binding = ItemMessageSelfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SelfMessageViewHolder(binding)
            }
            VIEW_TYPE_OTHER -> {
                val binding = ItemMessageOtherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                OtherMessageViewHolder(binding)
            }
            else -> {
                val binding = ItemSystemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SystemMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is DisplayItem.Header -> (holder as HeaderViewHolder).bind(item.text)
            is DisplayItem.MessageItem -> {
                val message = item.message
                when (holder) {
                    is SelfMessageViewHolder -> holder.bind(message)
                    is OtherMessageViewHolder -> holder.bind(message, profilesMap)
                    is SystemMessageViewHolder -> holder.bind(message)
                }
            }
        }
    }

    inner class SelfMessageViewHolder(private val binding: ItemMessageSelfBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun highlight() {
            val originalColor = binding.messageBubble.cardBackgroundColor
            binding.messageBubble.setCardBackgroundColor(
                androidx.core.content.ContextCompat.getColor(
                    binding.root.context, R.color.light_gray
                )
            )
            binding.messageBubble.postDelayed({
                binding.messageBubble.setCardBackgroundColor(originalColor)
            }, 500)
        }

        fun bind(message: Message) {
            message.parentMessageId?.let { parentId ->
                val parentMsg = getParentMessage(parentId)
                if (parentMsg != null) {
                    binding.replyPreview.visibility = View.VISIBLE
                    binding.replyText.text = parentMsg.content.take(100)
                } else {
                    binding.replyPreview.visibility = View.GONE
                }
            } ?: run { binding.replyPreview.visibility = View.GONE }

            binding.messageText.text = message.content
            binding.messageTime.text = DateUtils.formatToTime(message.createdAt)
            binding.messageEdited.visibility = if (message.isEdited) View.VISIBLE else View.GONE

            binding.root.setOnLongClickListener {
                onMessageLongClick(message, binding.root)
                true
            }

            binding.replyPreview.setOnClickListener {
                message.parentMessageId?.let { onReplyClick(it) }
            }
        }
    }

    inner class OtherMessageViewHolder(private val binding: ItemMessageOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun highlight() {
            val originalColor = binding.messageBubble.cardBackgroundColor
            binding.messageBubble.setCardBackgroundColor(
                androidx.core.content.ContextCompat.getColor(
                    binding.root.context, R.color.light_gray
                )
            )
            binding.messageBubble.postDelayed({
                binding.messageBubble.setCardBackgroundColor(originalColor)
            }, 500)
        }

        fun bind(message: Message, profiles: Map<String, Profile>) {
            val profile = profiles[message.senderId]

            message.parentMessageId?.let { parentId ->
                val parentMsg = getParentMessage(parentId)
                if (parentMsg != null) {
                    binding.replyPreview.visibility = View.VISIBLE
                    binding.replyText.text = parentMsg.content.take(100)
                } else {
                    binding.replyPreview.visibility = View.GONE
                }
            } ?: run { binding.replyPreview.visibility = View.GONE }

            binding.userName.text = profile?.username ?: message.senderId.take(8)
            binding.messageText.text = message.content
            binding.messageTime.text = DateUtils.formatToTime(message.createdAt)
            binding.messageEdited.visibility = if (message.isEdited) View.VISIBLE else View.GONE

            val avatarUrl = profile?.avatarUrl
            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(binding.avatar)
            } else {
                binding.avatar.setImageResource(R.drawable.ic_person)
            }

            binding.root.setOnLongClickListener {
                onMessageLongClick(message, binding.root)
                true
            }

            binding.replyPreview.setOnClickListener {
                message.parentMessageId?.let { onReplyClick(it) }
            }
        }
    }

    class SystemMessageViewHolder(private val binding: ItemSystemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.systemMessageText.text = message.content
        }
    }

    class HeaderViewHolder(private val binding: ItemMessageHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.headerText.text = text
        }
    }

    private class DisplayItemDiffCallback : DiffUtil.ItemCallback<DisplayItem>() {
        override fun areItemsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
            return when (oldItem) {
                is DisplayItem.Header if newItem is DisplayItem.Header -> oldItem.text == newItem.text
                is DisplayItem.MessageItem if newItem is DisplayItem.MessageItem -> oldItem.message.id == newItem.message.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
            return oldItem == newItem
        }
    }
}