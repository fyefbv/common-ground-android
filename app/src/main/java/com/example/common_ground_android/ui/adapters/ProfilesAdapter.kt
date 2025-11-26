package com.example.common_ground_android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.common_ground_android.R
import com.example.common_ground_android.data.models.Profile
import com.example.common_ground_android.databinding.ItemProfileBinding
import com.google.android.material.chip.Chip

class ProfilesAdapter(
    private val onProfileClick: (Profile) -> Unit
) : ListAdapter<Profile, ProfilesAdapter.ProfileViewHolder>(ProfileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ItemProfileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = getItem(position)
        holder.bind(profile)
    }

    inner class ProfileViewHolder(
        private val binding: ItemProfileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onProfileClick(getItem(position))
                }
            }
        }

        fun bind(profile: Profile) {
            binding.profileName.text = profile.name

            binding.interestsChipGroup.removeAllViews()

            profile.interests.take(3).forEach { interest ->
                val chip = Chip(binding.root.context).apply {
                    text = interest
                    isCheckable = false
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                        resources.getColor(R.color.md_secondary_container, null)
                    )
                    setTextColor(resources.getColor(R.color.md_on_secondary_container, null))
                    chipCornerRadius = resources.getDimension(R.dimen.corner_radius_full)
                }
                binding.interestsChipGroup.addView(chip)
            }

            if (profile.interests.size > 3) {
                val moreChip = Chip(binding.root.context).apply {
                    text = "+${profile.interests.size - 3}"
                    isCheckable = false
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                        resources.getColor(R.color.md_secondary_container, null)
                    )
                    setTextColor(resources.getColor(R.color.md_on_secondary_container, null))
                    chipCornerRadius = resources.getDimension(R.dimen.corner_radius_full)
                }
                binding.interestsChipGroup.addView(moreChip)
            }
        }
    }
}

class ProfileDiffCallback : DiffUtil.ItemCallback<Profile>() {
    override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean {
        return oldItem == newItem
    }
}