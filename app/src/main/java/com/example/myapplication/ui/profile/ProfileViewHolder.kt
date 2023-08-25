package com.example.myapplication.ui.profile

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentProfileBinding
import com.example.myapplication.model.ProfileModel
import de.hdodenhof.circleimageview.CircleImageView

class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(profileModel: ProfileModel) {
            val profilePhoto = itemView.findViewById(R.id.profileFragmentPhoto) as CircleImageView
            val userName = itemView.findViewById(R.id.profileName) as TextView


            Glide.with(itemView.context).load(profileModel.profilePhoto).into(profilePhoto);
            userName.text = profileModel.userName

        }
    }
