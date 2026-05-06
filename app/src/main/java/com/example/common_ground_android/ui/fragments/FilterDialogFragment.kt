package com.example.common_ground_android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentFilterDialogBinding
import com.example.common_ground_android.network.model.domain.interest.Interest
import com.example.common_ground_android.ui.viewmodels.rooms.RoomFilters
import com.example.common_ground_android.ui.viewmodels.rooms.RoomsViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class FilterDialogFragment : DialogFragment() {

    private var _binding: FragmentFilterDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoomsViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private lateinit var interestAdapter: ArrayAdapter<String>
    private lateinit var tagAdapter: ArrayAdapter<String>
    private lateinit var sortByAdapter: ArrayAdapter<String>
    private lateinit var sortOrderAdapter: ArrayAdapter<String>

    private val selectedInterestIds = mutableSetOf<String>()
    private val selectedTags = mutableSetOf<String>()
    private val allInterests = mutableListOf<Interest>()
    private val allTags = mutableListOf<String>()

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAutoCompleteForInterests()
        setupAutoCompleteForTags()
        setupSortSpinners()
        observeFilters()
        setupButtons()
    }

    private fun setupAutoCompleteForInterests() {
        interestAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.interestAutoComplete.setAdapter(interestAdapter)
        binding.interestAutoComplete.threshold = 1

        lifecycleScope.launch {
            viewModel.availableInterests.collect { interests ->
                allInterests.clear()
                allInterests.addAll(interests)
                interestAdapter.clear()
                interestAdapter.addAll(interests.map { it.name })
                interestAdapter.notifyDataSetChanged()
            }
        }

        binding.interestAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedName = interestAdapter.getItem(position)
            val interest = allInterests.find { it.name == selectedName }
            interest?.let {
                if (!selectedInterestIds.contains(it.id)) {
                    selectedInterestIds.add(it.id)
                    addInterestChip(it)
                }
                binding.interestAutoComplete.text?.clear()
            }
        }
    }

    private fun addInterestChip(interest: Interest) {
        val chip = Chip(requireContext()).apply {
            text = interest.name
            tag = interest.id
            isCloseIconVisible = true
            setCloseIconResource(R.drawable.ic_close)
            setOnCloseIconClickListener {
                selectedInterestIds.remove(interest.id)
                binding.selectedInterestsChipGroup.removeView(this)
            }
        }
        binding.selectedInterestsChipGroup.addView(chip)
    }

    private fun setupAutoCompleteForTags() {
        tagAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.tagAutoComplete.setAdapter(tagAdapter)
        binding.tagAutoComplete.threshold = 1

        lifecycleScope.launch {
            viewModel.availableTags.collect { tags ->
                allTags.clear()
                allTags.addAll(tags)
                tagAdapter.clear()
                tagAdapter.addAll(tags)
                tagAdapter.notifyDataSetChanged()
            }
        }

        binding.tagAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val tag = tagAdapter.getItem(position)
            tag?.let {
                if (!selectedTags.contains(it)) {
                    selectedTags.add(it)
                    addTagChip(it)
                }
                binding.tagAutoComplete.text?.clear()
            }
        }
    }

    private fun addTagChip(tagText: String) {
        val chip = Chip(requireContext()).apply {
            text = tagText
            tag = tagText
            isCloseIconVisible = true
            setCloseIconResource(R.drawable.ic_close)
            setOnCloseIconClickListener {
                selectedTags.remove(tagText)
                binding.selectedTagsChipGroup.removeView(this)
            }
        }
        binding.selectedTagsChipGroup.addView(chip)
    }

    private fun setupSortSpinners() {
        sortByAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf(
            getString(R.string.date),
            getString(R.string.participants)
        ))
        binding.sortBySpinner.setAdapter(sortByAdapter)
        binding.sortBySpinner.setOnClickListener { binding.sortBySpinner.showDropDown() }
        binding.sortBySpinner.setOnItemClickListener { _, _, position, _ ->
            binding.sortBySpinner.setText(sortByAdapter.getItem(position), false)
        }

        sortOrderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf(
            getString(R.string.descending),
            getString(R.string.ascending)
        ))
        binding.sortOrderSpinner.setAdapter(sortOrderAdapter)
        binding.sortOrderSpinner.setOnClickListener { binding.sortOrderSpinner.showDropDown() }
        binding.sortOrderSpinner.setOnItemClickListener { _, _, position, _ ->
            binding.sortOrderSpinner.setText(sortOrderAdapter.getItem(position), false)
        }
    }

    private fun observeFilters() {
        lifecycleScope.launch {
            viewModel.filters.collect { filters ->
                if (isAdded) {
                    selectedInterestIds.clear()
                    selectedInterestIds.addAll(filters.interestIds)
                    binding.selectedInterestsChipGroup.removeAllViews()
                    allInterests.filter { it.id in filters.interestIds }.forEach { addInterestChip(it) }

                    selectedTags.clear()
                    selectedTags.addAll(filters.tags)
                    binding.selectedTagsChipGroup.removeAllViews()
                    allTags.filter { it in filters.tags }.forEach { addTagChip(it) }

                    binding.allRooms.isChecked = !filters.myRooms
                    binding.myRooms.isChecked = filters.myRooms

                    val sortByText = when (filters.sortBy) {
                        "participants" -> getString(R.string.participants)
                        else -> getString(R.string.date)
                    }
                    binding.sortBySpinner.setText(sortByText, false)

                    val sortOrderText = when (filters.sortOrder) {
                        "asc" -> getString(R.string.ascending)
                        else -> getString(R.string.descending)
                    }
                    binding.sortOrderSpinner.setText(sortOrderText, false)
                }
            }
        }
    }

    private fun setupButtons() {
        binding.resetButton.setOnClickListener {
            selectedInterestIds.clear()
            binding.selectedInterestsChipGroup.removeAllViews()
            selectedTags.clear()
            binding.selectedTagsChipGroup.removeAllViews()
            binding.allRooms.isChecked = true
            binding.sortBySpinner.setText(getString(R.string.date), false)
            binding.sortOrderSpinner.setText(getString(R.string.descending), false)
            viewModel.resetFilters()
            dismiss()
        }
        binding.applyButton.setOnClickListener {
            val myRooms = binding.myRooms.isChecked
            val sortBy = when (binding.sortBySpinner.text.toString()) {
                getString(R.string.participants) -> "participants"
                else -> "created_at"
            }
            val sortOrder = when (binding.sortOrderSpinner.text.toString()) {
                getString(R.string.ascending) -> "asc"
                else -> "desc"
            }
            val newFilters = RoomFilters(
                query = viewModel.filters.value.query,
                interestIds = selectedInterestIds.toSet(),
                tags = selectedTags.toSet(),
                myRooms = myRooms,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
            viewModel.updateFilters(newFilters)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}