package br.com.scrubs

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import br.com.scrubs.presentation.biometric.ActivityProvider

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        ActivityProvider.set(this)  // registra a activity ativa
    }

    override fun onPause() {
        super.onPause()
        ActivityProvider.clear()    // evita vazamento de memória
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}