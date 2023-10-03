package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentNewMessageBinding
import com.example.myapplication.databinding.FragmentSearchBinding
import com.example.myapplication.model.UserModel
import com.example.myapplication.ui.adapter.NewMessageAdapter
import com.example.myapplication.ui.adapter.SearchAdapter
import com.example.myapplication.ui.search.SearchFragmentDirections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase


class NewMessageFragment : Fragment() {

    private lateinit var _binding: FragmentNewMessageBinding
    private val binding get() = _binding!!
    private lateinit var newMessageAdapter: NewMessageAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    var searchData: ArrayList<UserModel>? = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewMessageBinding.inflate(inflater, container, false)
        val view = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        if (context != null) {
            setupRecyclerView()
        }

        return view
    }

    private fun setupRecyclerView() {
        newMessageAdapter = NewMessageAdapter(
            requireContext(),
            searchData!!,
            NewMessageAdapter.OnClickListener { searchItem ->
                val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("clickedUserId", searchItem.userId)
                    apply()
                }

                val bundle = Bundle()
                bundle.putParcelable("clickedUserIdModel", searchItem) // UserModel'ı parcelable olarak bundle'a ekliyoruz

                val chatFragment = ChatFragment()
                chatFragment.arguments = bundle
                val fragmentTransaction = parentFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragment_container, chatFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            })

        binding.newMessageRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newMessageAdapter
            setHasFixedSize(true)
        }
    }
                override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            val messageFragment = MessageFragment()
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.searchPage, messageFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }


        binding.messageView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.messageView.text.toString() == "") {
                    binding.newMessageRV.visibility = View.GONE

                    searchData?.clear()
                } else {
                    binding.newMessageRV.visibility = View.VISIBLE

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
                                searchData?.let { newMessageAdapter.submitMessageList(it) }

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