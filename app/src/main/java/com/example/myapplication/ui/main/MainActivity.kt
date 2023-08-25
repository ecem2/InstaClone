package com.example.myapplication.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val listener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.storyClickFragment -> {
                    val navBar: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    navBar.visibility = View.GONE
                }
                R.id.userProfileFragment -> {
                    val navBar: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    navBar.visibility = View.GONE
                }
                R.id.cameraFragment -> {
                    val navBar: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    navBar.visibility = View.GONE
                }
                R.id.messageFragment -> {
                    val navBar: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    navBar.visibility = View.GONE
                }

                else -> {
                    val navBar: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    navBar.visibility = View.VISIBLE
                }

            }

        }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        navController.removeOnDestinationChangedListener(listener)
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupWithNavController(binding.bottomNavigationView, navController)

    }


}