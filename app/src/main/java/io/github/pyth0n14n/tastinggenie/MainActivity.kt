package io.github.pyth0n14n.tastinggenie

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import dagger.hilt.android.AndroidEntryPoint
import io.github.pyth0n14n.tastinggenie.navigation.AppNavGraph
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingGenie2ndAndroidTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TastingGenie2ndAndroidTheme {
                val backgroundColor = MaterialTheme.colorScheme.background
                SideEffect {
                    window.setBackgroundDrawable(
                        ColorDrawable(backgroundColor.toArgb()),
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor,
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}
