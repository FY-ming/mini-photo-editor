package com.example.mini_photo_editor.ui


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.mini_photo_editor.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // 直接设置布局

        setupNavigation()
    }

    private fun setupNavigation() {
        // 直接通过findViewById获取控件
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)

        bottomNavigation.setupWithNavController(navController)
    }
}