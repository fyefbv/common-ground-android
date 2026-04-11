package com.example.common_ground_android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.ItemProfileBinding
import com.example.common_ground_android.network.model.domain.profile.Profile
import com.google.android.material.chip.Chip

class ProfileAdapter(
    private val profiles: List<Profile>,
    private val onProfileSelected: (Profile) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ItemProfileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    override fun getItemCount(): Int = profiles.size

    inner class ProfileViewHolder(
        private val binding: ItemProfileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: Profile) {
            binding.profileName.text = profile.username
            binding.profileBio.text = profile.bio ?: ""

            Glide.with(binding.root.context)
                .load(profile.avatarUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(binding.profileAvatar)

            binding.interestsChipGroup.removeAllViews()
            profile.interests.forEach { interest ->
                val chip = Chip(binding.root.context).apply {
                    text = interest.name
                    isClickable = false
                    isCheckable = false
                    setChipBackgroundColorResource(R.color.md_surface_container)
                    setTextColor(ContextCompat.getColor(context, R.color.md_on_surface_variant))
                }
                binding.interestsChipGroup.addView(chip)
            }

            binding.root.setOnClickListener {
                onProfileSelected(profile)
            }
        }
    }
}