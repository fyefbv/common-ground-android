package com.example.common_ground_android.ui.fragments.chat_roulette

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentReportDialogBinding
import com.example.common_ground_android.ui.viewmodels.chat_roulette.ChatRouletteViewModel
import com.example.common_ground_android.ui.viewmodels.chat_roulette.ChatRouletteViewModelFactory
import com.google.android.material.chip.Chip

class ReportDialogFragment : DialogFragment() {

    private var _binding: FragmentReportDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatRouletteViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    ) {
        ChatRouletteViewModelFactory(requireContext())
    }
    private var selectedReason: String? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reasonChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = group.findViewById<Chip>(checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener)
            selectedReason = chip.text.toString()
            binding.detailsEditText.error = null
        }
        binding.submitReportButton.setOnClickListener {
            if (selectedReason == null) {
                binding.detailsEditText.error = getString(R.string.select_reason)
                return@setOnClickListener
            }
            val details = binding.detailsEditText.text.toString().trim()
            if (details.isNotEmpty() && details.length < 20) {
                binding.detailsEditText.error = getString(R.string.details_min_length)
                return@setOnClickListener
            }
            viewModel.reportPartner(selectedReason!!, details.takeIf { it.isNotEmpty() })
            dismiss()
        }
        binding.cancelReportButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}