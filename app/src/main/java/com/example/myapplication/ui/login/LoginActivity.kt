package com.example.myapplication.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.ui.main.MainActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging


class LoginActivity : AppCompatActivity() {

    lateinit var btn: Button
    lateinit var kulAd: EditText
    lateinit var pass: EditText
    private lateinit var binding: ActivityLoginBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    var user: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        btn = findViewById(R.id.btnLogin)
        kulAd = findViewById(R.id.loginNickname)
        pass = findViewById(R.id.loginPass)
        databaseReference = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        user = FirebaseAuth.getInstance().currentUser?.uid
        firebaseAuth = FirebaseAuth.getInstance()
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener<String?> { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "FCM registration token failed", task.exception)

                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Log and toast
                val msg = token
                Log.d("Tokennn", "$msg")
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                val tokenRef = user?.let { databaseReference.child("Token").child(it) }
                val tokenMap = HashMap<String, Any>()
                tokenMap["token"] = token
                tokenRef?.updateChildren(tokenMap)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Ecccoo", "SUCCESSFUL === ${task.isSuccessful}")
                        } else {
                            Log.d("Ecccoo", "ERROR")
                            Toast.makeText(this, "Post could not load", Toast.LENGTH_SHORT).show()
                        }
                    }
                    ?.addOnFailureListener { exception ->
                        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                        Log.d("Ecccoo", "FAILURE === ${exception.message}")
                    }

            })


        binding.registerTV.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val kulAd = binding.loginNickname.text.toString()
            val pass = binding.loginPass.text.toString()

            val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
            val editor = sharedPref.edit()

            editor.apply {
                putString("user_name", kulAd)
                putString("email", pass)
                apply()

            }


//            val intent = Intent(this, RegisterActivity::class.java)
//            intent.putExtra("kullanıcıadı", binding.loginNickname.text.toString())
//            intent.putExtra("şifre", binding.loginPass.text.toString())
//            startActivity(intent)
//            finish()


            if (kulAd.isNotEmpty() && pass.isNotEmpty()) {
                val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
                val editor = sharedPref.edit()

                firebaseAuth.signInWithEmailAndPassword(kulAd, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val userPref = getSharedPreferences("USER_ID", MODE_PRIVATE).edit()
                        userPref.putString("USER_ID", it.result.user?.uid)
                        userPref.apply()


                        editor.apply {
                            putBoolean("isUserLogin", true)
                            editor.commit()
                        }
                        // TODO: locale login olduğunu bildir
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        editor.apply {
                            putBoolean("isUserLogin", false)
                            editor.commit()
                        }
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

            }
        }
    }
    companion object {
        private const val TAG = "EcemLoginAct"
    }
}