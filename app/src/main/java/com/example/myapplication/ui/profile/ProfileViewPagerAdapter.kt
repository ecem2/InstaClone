package com.example.myapplication.ui.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.myapplication.ui.users.UserProfilePostFragment
import com.example.myapplication.ui.users.UserProfileReelsFragment
import com.example.myapplication.ui.users.UserProfileTaggetPostFragment

class ProfileViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                UserProfilePostFragment()
            }
            1 -> {
                UserProfileReelsFragment()
            }
            2 -> {
                UserProfileTaggetPostFragment()
            }
            else -> {
                UserProfilePostFragment()
            }
        }
    }
}