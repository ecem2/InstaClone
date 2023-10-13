package com.example.myapplication.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentSearchBinding
import com.example.myapplication.model.UserModel
import com.example.myapplication.ui.adapter.SearchAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class SearchFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    var searchData: ArrayList<UserModel>? = ArrayList()
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var _binding: FragmentSearchBinding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        if (context != null) {
            setupRecyclerView()
        }

        return view

    }


    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(
            requireContext(),
            searchData!!,
            SearchAdapter.OnClickListener { searchItem ->
//                val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
//                Log.d("ecemmm", "search ${searchItem}")
//                with(sharedPref.edit()) {
//                    putString("clickedUserId", searchItem)
//                    apply()
//                }



                val action =
                    SearchFragmentDirections.actionSearchFragmentToUserProfileFragment(searchItem)
                findNavController().navigate(action)
            })

        binding.searchRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
            setHasFixedSize(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.searchView.text.toString() == "") {
                    binding.searchRV.visibility = View.GONE

                    searchData?.clear()
                } else {
                    binding.searchRV.visibility = View.VISIBLE

                    database.child("Users")
                        .orderByChild("userNickName")
                            .startAt(s.toString().lowercase())
                        .endAt(s.toString().lowercase() + "\uf8ff")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshotUser: DataSnapshot) {
                                for (i in snapshotUser.children) {
                                    val search = i.getValue<UserModel>()
                                    if (search != null) {
                                        searchData?.add(search)
                                    }
                                }
                                searchData?.let { searchAdapter.submitSearchList(it) }

                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }
}