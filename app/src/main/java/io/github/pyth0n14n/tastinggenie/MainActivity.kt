package io.github.pyth0n14n.tastinggenie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
                AppNavGraph()
            }
        }
    }
}
