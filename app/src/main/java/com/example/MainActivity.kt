package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.CafeAppScreen
import com.example.ui.CafeViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Always call enableEdgeToEdge to support seamless modern devices
        enableEdgeToEdge()
        
        // Singletons setup
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(database.appDao)
        
        // ViewModel Factory definition
        val factory = CafeViewModelFactory(application, repository)
        val viewModel: CafeViewModel by viewModels { factory }
        
        setContent {
            MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
                CafeAppScreen(viewModel = viewModel)
            }
        }
    }
}

class CafeViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CafeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CafeViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
