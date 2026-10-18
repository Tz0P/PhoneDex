package com.phonedex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.phonedex.app.ui.MainViewModel
import com.phonedex.app.ui.PhoneDexRoot
import com.phonedex.app.ui.theme.PhoneDexTheme

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels {
        viewModelFactory {
            initializer { MainViewModel((application as PhoneDexApplication).repository) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PhoneDexTheme { PhoneDexRoot(vm) } }
    }
}
