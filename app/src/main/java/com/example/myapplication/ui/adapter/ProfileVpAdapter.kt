package com.example.myapplication.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.myapplication.ui.profile.ProfilePostFragment
import com.example.myapplication.ui.profile.ProfileReelsFragment
import com.example.myapplication.ui.profile.ProfileTaggetFragment
import com.example.myapplication.ui.users.UserProfilePostFragment
import com.example.myapplication.ui.users.UserProfileReelsFragment
import com.example.myapplication.ui.users.UserProfileTaggetPostFragment

class ProfileVpAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                ProfilePostFragment()
            }
            1 -> {
                ProfileReelsFragment()
            }
            2 -> {
                ProfileTaggetFragment()
            }
            else -> {
                ProfilePostFragment()
            }
        }
    }
}