package com.example.myapplication.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    lateinit var btn: Button
    lateinit var kulAd: EditText
    lateinit var pass: EditText
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        btn = findViewById(R.id.btnLogin)
        kulAd = findViewById(R.id.loginNickname)
        pass = findViewById(R.id.loginPass)

        firebaseAuth = FirebaseAuth.getInstance()


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

    override fun onStart() {
        super.onStart()

//        if (firebaseAuth.currentUser != null) {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }
    }
}