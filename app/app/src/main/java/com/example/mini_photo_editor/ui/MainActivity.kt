package com.example.mini_photo_editor.ui


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.mini_photo_editor.R

class MainActivity : AppCompatActivity() {
    // 主函数，程序入口

    override fun onCreate(savedInstanceState: Bundle?) {
        // 创建窗口（初始化函数）
        super.onCreate(savedInstanceState)
        // 设置 Activity 的界面布局，使用 activity_main.xml
        setContentView(R.layout.activity_main)  // 直接设置布局

        // 初始化底部导航栏并绑定 NavController
        setupNavigation()
    }

    private fun setupNavigation() {
        // 直接通过findViewById精准获取 BottomNavigationView（主页底部导航栏组件实例）
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        // 直接通过findViewById获取 NavController（控制导航NavHostFragment 的容器 ID）
        val navController = findNavController(R.id.nav_host_fragment)

        // 将底部导航栏与 NavController 绑定
        // 这样点击底部菜单时会自动切换对应的 Fragment
        bottomNavigation.setupWithNavController(navController)
    }
}