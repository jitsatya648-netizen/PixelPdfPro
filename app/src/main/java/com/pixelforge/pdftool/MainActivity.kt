package com.pixelforge.pdftool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowInsetsControllerCompat
import com.pixelforge.pdftool.ui.screens.HomeScreen
import com.pixelforge.pdftool.ui.screens.SplashScreen
import com.pixelforge.pdftool.ui.theme.AppTheme
import com.pixelforge.pdftool.ui.vm.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF07070F)) {
                    var splashFinished by remember { mutableStateOf(false) }
                    AnimatedContent(
                        targetState = splashFinished,
                        transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) },
                        label = "RootTransition"
                    ) { finished ->
                        if (!finished) {
                            SplashScreen(onFinished = { splashFinished = true })
                        } else {
                            HomeScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}