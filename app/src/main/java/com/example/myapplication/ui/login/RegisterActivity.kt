package com.example.myapplication.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginTV.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnRegister.setOnClickListener {
            val telNo = binding.registerPhone.text.toString()
            val name = binding.registerName.text.toString().trim()
            val pass = binding.registerPass.text.toString()
            val confirmPass = binding.passConfirm.text.toString()

            if (telNo.isNotEmpty() && name.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(telNo, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            addDataToFirebase(
                                firebaseAuth.currentUser!!.uid,
                                name,
                                telNo,
                                confirmPass
                            )
                            Log.d(TAG, "USER CREATED SUCCESSFULLY")
                        } else {
                            Log.e(TAG, "User NOT registered ${it.exception?.localizedMessage}")
                        }
                    }.addOnFailureListener {
                        Log.e(TAG, "REGISTER FAILURE ${it.localizedMessage}")
                    }
                } else {
                    Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
        fun printInputText(text: String) {
            Log.i("fatosss", "INPUT TEXT: ${text}")
            Log.d("fatosss", "INPUT TEXT222: ${text}")
            Log.e("fatosss", "INPUT TEXT333: ${text}")
        }


        private fun addDataToFirebase(userUid: String, name: String, email: String, nickname: String
        ) {
            val createdUser: HashMap<String, Any> = HashMap()
            createdUser["userId"] = userUid
            createdUser["userName"] = name
            createdUser["userEmail"] = email
            createdUser["userNickName"] = nickname

            databaseReference.child("Users").child(firebaseAuth.currentUser!!.uid)
                .updateChildren(createdUser).addOnSuccessListener {
                    navigateToLogin()
                }.addOnFailureListener {
                    Log.e(TAG, "ECEM HATA == ${it.localizedMessage}")
                }
        }

        private fun navigateToLogin() {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        companion object {
            private const val TAG = "EcemRegisterAct"
        }



}