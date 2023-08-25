package com.example.myapplication.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.CameraFragment
import com.example.myapplication.HomePageFragment
import com.example.myapplication.MessageFragment
import com.example.myapplication.ui.home.HomeFragment

class HomeViewPagerAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CameraFragment()

            1 -> HomeFragment()

            2 -> MessageFragment()

            else -> HomeFragment()
        }
    }
}