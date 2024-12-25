package com.example.tencentWork

import android.content.Context

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.annotation.RequiresApi

import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.tencentWork.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化全局上下文
        appContext = applicationContext

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //定义导航组件和相关fragment组件
        val navView: BottomNavigationView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        // If the fragment is found and is of type NavHostFragment
        val navController = navHostFragment.navController

        // Set up BottomNavigationView with NavController
        navView.setupWithNavController(navController)

    }

    //注册全局上下文
    companion object {
        lateinit var appContext: Context
            private set
    }
}