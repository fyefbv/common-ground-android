package com.example.common_ground_android.ui.fragments.chat_roulette

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentRatePartnerDialogBinding
import com.example.common_ground_android.ui.viewmodels.chat_roulette.ChatRouletteViewModel
import com.example.common_ground_android.ui.viewmodels.chat_roulette.ChatRouletteViewModelFactory

class RatePartnerDialogFragment : DialogFragment() {

    private var _binding: FragmentRatePartnerDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatRouletteViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    ) {
        ChatRouletteViewModelFactory(requireContext())
    }
    private var rating = 0

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
        _binding = FragmentRatePartnerDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        binding.submitRatingButton.setOnClickListener {
            if (rating > 0) {
                val feedback = binding.feedbackEditText.text.toString().trim()
                viewModel.ratePartner(rating, feedback.takeIf { it.isNotEmpty() })
                viewModel.finishSession()
                dismiss()
            } else {
                binding.ratingText.text = "Выберите оценку"
            }
        }
        binding.skipRatingButton.setOnClickListener {
            viewModel.finishSession()
            dismiss()
        }

        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        stars.forEachIndexed { index, star ->
            star.setOnClickListener { setRating(index + 1, stars) }
        }
    }

    private fun setRating(value: Int, stars: List<ImageView>) {
        rating = value
        stars.forEachIndexed { i, star ->
            star.setImageResource(if (i < rating) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
        }
        binding.ratingText.text = "Оценка: $rating/5"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}