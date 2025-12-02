package com.example.common_ground_android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.common_ground_android.R
import com.example.common_ground_android.data.models.Room
import com.example.common_ground_android.databinding.FragmentHomeBinding
import com.example.common_ground_android.ui.adapters.RoomsAdapter

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var roomsAdapter: RoomsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        updateUI()
    }

    private fun setupRecyclerView() {
        roomsAdapter = RoomsAdapter { roomId ->
//            navigateToGroupRoom(roomId)
        }

        binding.roomsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = roomsAdapter
        }

        loadRooms()
    }

    private fun setupClickListeners() {
        binding.chatRouletteCard.setOnClickListener {
            navigateToChatRoulette()
        }

        binding.profileButton.setOnClickListener {
            navigateToProfile()
        }

        binding.switchProfileButton.setOnClickListener {
            navigateToProfileSelector()
        }
    }

    private fun updateUI() {
        binding.userNameText.text = "Александр"

        binding.switchProfileButton.visibility = View.VISIBLE

        binding.roomsCountChip.text = getString(R.string.rooms_count, 8)
    }

    private fun loadRooms() {
        val rooms = listOf(
            Room(
                "1",
                "Технологии и AI",
                "Технологии",
                24,
                "Обсуждаем новейшие технологии и искусственный интеллект"
            ),
            Room("2", "Музыкальная гостиная", "Музыка", 18, "Делимся любимой музыкой и обсуждаем новинки"),
            Room("3", "Путешественники", "Путешествия", 32, "Рассказываем о путешествиях и планируем новые"),
            Room("4", "Киноманы", "Кино", 15, "Обсуждаем фильмы, сериалы и кинематограф")
        )
        roomsAdapter.submitList(rooms)
    }

    private fun navigateToChatRoulette() {
        findNavController().navigate(R.id.action_homeFragment_to_chatRouletteFragment)
    }

//    private fun navigateToGroupRoom(roomId: String) {
//        val action = HomeFragmentDirections.actionHomeFragmentToGroupRoomFragment(roomId)
//        findNavController().navigate(action)
//    }

    private fun navigateToProfile() {
        findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
    }

    private fun navigateToProfileSelector() {
        findNavController().navigate(R.id.action_homeFragment_to_profileSelectorFragment)
    }
}