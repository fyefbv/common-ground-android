package com.example.common_ground_android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.common_ground_android.R
import com.example.common_ground_android.data.models.ChatMessage
import com.example.common_ground_android.data.models.ChatPartner
import com.example.common_ground_android.databinding.FragmentChatRouletteBinding
import com.example.common_ground_android.ui.adapters.ChatMessagesAdapter
import java.util.*
import kotlin.concurrent.schedule

class ChatRouletteFragment : Fragment() {
    private lateinit var binding: FragmentChatRouletteBinding
    private lateinit var messagesAdapter: ChatMessagesAdapter

    private var isSearching = true
    private var currentPartner: ChatPartner? = null
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatRouletteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        startSearching()
    }

    private fun setupRecyclerView() {
        messagesAdapter = ChatMessagesAdapter()
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = messagesAdapter
        }
    }

    private fun setupClickListeners() {
        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.messageEditText.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }

    private fun startSearching() {
        isSearching = true
        currentPartner = null
        messages.clear()
        messagesAdapter.submitList(emptyList())

        binding.searchingState.visibility = View.VISIBLE
        binding.chatState.visibility = View.GONE

        Timer().schedule(2000) {
            requireActivity().runOnUiThread {
                foundPartner()
            }
        }
    }

    private fun foundPartner() {
        isSearching = false

        currentPartner = ChatPartner(
            name = "Алексей",
            interests = listOf("Технологии", "Музыка")
        )

        messages.add(ChatMessage.createPartnerMessage(
            text = "Привет! Рад познакомиться!"
        ))

        updatePartnerInfo()
        messagesAdapter.submitList(messages.toList())

        binding.searchingState.visibility = View.GONE
        binding.chatState.visibility = View.VISIBLE

        scrollToBottom()
    }

    private fun updatePartnerInfo() {
        currentPartner?.let { partner ->
            binding.partnerName.text = partner.name

            binding.partnerInterestsChipGroup.removeAllViews()

            partner.interests.forEach { interest ->
                val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                    text = interest
                    isCheckable = false
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                        resources.getColor(R.color.md_secondary_container, null)
                    )
                    setTextColor(resources.getColor(R.color.md_on_secondary_container, null))
                    chipCornerRadius = resources.getDimension(R.dimen.corner_radius_full)
                    textSize = 12f
                }
                binding.partnerInterestsChipGroup.addView(chip)
            }
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageEditText.text?.toString()?.trim()
        if (!messageText.isNullOrEmpty()) {
            val message = ChatMessage.createUserMessage(messageText)

            messages.add(message)
            messagesAdapter.submitList(messages.toList())
            binding.messageEditText.text?.clear()

            scrollToBottom()

            simulatePartnerResponse()
        }
    }

    private fun simulatePartnerResponse() {
        Timer().schedule(1000) {
            requireActivity().runOnUiThread {
                val responses = listOf(
                    "Интересно! Расскажи подробнее",
                    "Согласен, это действительно так",
                    "А у меня был похожий опыт",
                    "Отличная мысль!"
                )
                val randomResponse = responses.random()

                messages.add(ChatMessage.createPartnerMessage(text = randomResponse))
                messagesAdapter.submitList(messages.toList())
                scrollToBottom()
            }
        }
    }

    private fun scrollToBottom() {
        binding.messagesRecyclerView.post {
            binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
        }
    }
}