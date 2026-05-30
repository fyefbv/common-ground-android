package com.example.common_ground_android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.ItemParticipantBinding
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.Participant
import com.example.common_ground_android.network.model.domain.ParticipantRole

class ParticipantAdapter(
    private val onParticipantClick: (Participant) -> Unit,
    private val onMenuClick: (Participant) -> Unit,
    private val currentProfileId: String,
    private var currentUserRole: ParticipantRole = ParticipantRole.MEMBER
) : ListAdapter<Participant, ParticipantAdapter.ParticipantViewHolder>(ParticipantDiffCallback()) {

    private var profilesMap: Map<String, Profile> = emptyMap()

    fun updateProfiles(profiles: Map<String, Profile>) {
        profilesMap = profiles
        notifyItemRangeChanged(0, itemCount)
    }

    fun updateCurrentUserRole(role: ParticipantRole) {
        currentUserRole = role
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding = ItemParticipantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ParticipantViewHolder(private val binding: ItemParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: Participant) {
            val profile = profilesMap[participant.profileId]
            binding.participantName.text = profile?.username ?: participant.profileId.take(8)

            val roleText = when (participant.role) {
                ParticipantRole.CREATOR -> "Создатель"
                ParticipantRole.MODERATOR -> "Модератор"
                else -> "Участник"
            }
            binding.participantRoleChip.text = roleText

            if (participant.isBanned) {
                binding.participantStatusChip.visibility = View.VISIBLE
                binding.participantStatusChip.text = "Забанен"
                binding.participantStatusChip.setChipBackgroundColorResource(R.color.md_error_container)
                binding.participantStatusChip.setTextColor(ContextCompat.getColor(binding.root.context, R.color.md_on_error_container))
            } else if (participant.isMuted) {
                binding.participantStatusChip.visibility = View.VISIBLE
                binding.participantStatusChip.text = "Замучен"
                binding.participantStatusChip.setChipBackgroundColorResource(R.color.md_secondary_container)
                binding.participantStatusChip.setTextColor(ContextCompat.getColor(binding.root.context, R.color.md_on_secondary_container))
            } else {
                binding.participantStatusChip.visibility = View.GONE
            }

            val isOnline = participant.isOnline
            binding.onlineStatusContainer.visibility = if (isOnline) View.VISIBLE else View.GONE
            binding.offlineStatusContainer.visibility = if (!isOnline) View.VISIBLE else View.GONE

            Glide.with(binding.root.context)
                .load(profile?.avatarUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(binding.participantAvatar)

            val isSelf = participant.profileId == currentProfileId
            val canModerate = when (currentUserRole) {
                ParticipantRole.CREATOR -> true
                ParticipantRole.MODERATOR -> participant.role == ParticipantRole.MEMBER
                else -> false
            }
            binding.participantMenuButton.visibility = if (canModerate && !isSelf) View.VISIBLE else View.GONE
            binding.participantMenuButton.setOnClickListener { onMenuClick(participant) }

            binding.root.setOnClickListener { onParticipantClick(participant) }
        }
    }

    private class ParticipantDiffCallback : DiffUtil.ItemCallback<Participant>() {
        override fun areItemsTheSame(oldItem: Participant, newItem: Participant): Boolean =
            oldItem.profileId == newItem.profileId
        override fun areContentsTheSame(oldItem: Participant, newItem: Participant): Boolean =
            oldItem == newItem
    }
}