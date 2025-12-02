package com.example.common_ground_android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.common_ground_android.R
import com.example.common_ground_android.data.models.Room
import com.example.common_ground_android.databinding.ItemRoomBinding

class RoomsAdapter(
    private val onRoomClick: (String) -> Unit
) : ListAdapter<Room, RoomsAdapter.RoomViewHolder>(RoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = getItem(position)
        holder.bind(room)
    }

    inner class RoomViewHolder(
        private val binding: ItemRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.joinButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRoomClick(getItem(position).id)
                }
            }

            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRoomClick(getItem(position).id)
                }
            }
        }

        fun bind(room: Room) {
            binding.roomName.text = room.name
            binding.roomDescription.text = room.description
            binding.topicChip.text = room.topic
            binding.membersCount.text = binding.root.context.getString(R.string.members, room.members)
        }
    }
}

class RoomDiffCallback : DiffUtil.ItemCallback<Room>() {
    override fun areItemsTheSame(oldItem: Room, newItem: Room): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Room, newItem: Room): Boolean {
        return oldItem == newItem
    }
}