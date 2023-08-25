package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.databinding.FragmentHomePageBinding
import com.example.myapplication.databinding.FragmentUserPostBinding
import com.example.myapplication.ui.adapter.HomeViewPagerAdapter

class HomePageFragment : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        val view = binding.root

        setupViewPager()

        return view
    }

    private fun setupViewPager() {
        val viewPager: ViewPager2 = binding.homeViewPager
        viewPager.adapter = HomeViewPagerAdapter(requireActivity())
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        //viewPager.setCurrentItem(1, false)
        val initialFragmentIndex = 1
        viewPager.currentItem = initialFragmentIndex
    }
}