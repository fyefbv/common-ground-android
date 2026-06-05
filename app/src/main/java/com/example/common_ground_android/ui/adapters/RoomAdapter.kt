package com.example.common_ground_android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.ItemRoomBinding
import com.example.common_ground_android.network.model.domain.Room
import com.google.android.material.chip.Chip

class RoomAdapter(
    private val currentProfileId: String,
    private val getInterestName: (String?) -> String?,
    private val onJoinClick: (Room) -> Unit,
    private val onLeaveClick: (Room) -> Unit,
    private val onRoomClick: (Room) -> Unit
) : ListAdapter<Room, RoomAdapter.RoomViewHolder>(RoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoomViewHolder(private val binding: ItemRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(room: Room) {
            binding.roomName.text = room.name
            binding.roomDescription.text = room.description ?: ""
            binding.participantsText.text = "${room.participantsCount}/${room.maxParticipants}"

            val interestName = getInterestName(room.primaryInterestId)
            binding.primaryInterestChip.text = interestName ?: room.primaryInterestId ?: binding.root.context.getString(R.string.no_interest)
            binding.primaryInterestChip.visibility = if (interestName != null || room.primaryInterestId != null) View.VISIBLE else View.GONE

            binding.privacyChip.text = if (room.isPrivate) binding.root.context.getString(R.string.private_room) else binding.root.context.getString(R.string.public_room)
            binding.privacyChip.setChipBackgroundColorResource(
                if (room.isPrivate) R.color.md_secondary_container
                else R.color.md_tertiary_container
            )

            binding.tagsChipGroup.removeAllViews()
            room.tags.forEach { tag ->
                val chip = Chip(binding.root.context).apply {
                    text = tag
                    isClickable = false
                    isCheckable = false
                    setChipBackgroundColorResource(R.color.md_surface_container_high)
                    setTextColor(ContextCompat.getColor(context, R.color.md_on_surface_variant))
                }
                binding.tagsChipGroup.addView(chip)
            }

            if (room.isBanned) {
                binding.joinButton.visibility = View.GONE
                binding.leaveButton.visibility = View.GONE
            } else {
                val isCreator = room.creatorId == currentProfileId
                if (isCreator) {
                    binding.joinButton.visibility = View.GONE
                    binding.leaveButton.visibility = View.GONE
                } else {
                    binding.joinButton.visibility = if (room.isJoined) View.GONE else View.VISIBLE
                    binding.leaveButton.visibility = if (room.isJoined) View.VISIBLE else View.GONE
                }
            }

            binding.joinButton.setOnClickListener { onJoinClick(room) }
            binding.leaveButton.setOnClickListener { onLeaveClick(room) }

            binding.root.setOnClickListener {
                if (room.isBanned) {
                    Toast.makeText(binding.root.context, R.string.you_are_banned_in_this_room, Toast.LENGTH_SHORT).show()
                } else if (!room.isJoined && room.creatorId != currentProfileId) {
                    Toast.makeText(binding.root.context, R.string.you_are_not_member_click_join, Toast.LENGTH_SHORT).show()
                } else {
                    onRoomClick(room)
                }
            }
        }
    }

    private class RoomDiffCallback : DiffUtil.ItemCallback<Room>() {
        override fun areItemsTheSame(oldItem: Room, newItem: Room): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Room, newItem: Room): Boolean = oldItem == newItem
    }
}