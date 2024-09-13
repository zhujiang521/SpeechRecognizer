package com.zj.speech

import android.Manifest.permission
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.zj.speech.ui.theme.SpeechRecognizerTheme
import com.zui.translator.utils.LANGUAGE_CHINESE
import com.zui.translator.SpeechRecognizerUtils
import com.zui.translator.model.SpeechModel

class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermission()
        setContent {
            SpeechRecognizerTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    bottomBar = { BottomBar() }) { padding ->
                    SpeechText(padding)
                }
            }
        }
    }

    @Composable
    private fun SpeechText(padding: PaddingValues) {
        val speechModel by mainViewModel.speechFlow.collectAsState(
            SpeechModel()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text("听到的语音")
            Text("${speechModel.event?.result?.text}")
            Text("翻译的结果")
            Text("${speechModel.translatorModel?.get(0)?.translations?.get(0)?.text}")
        }
    }

    @Composable
    private fun BottomBar() {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = {
                mainViewModel.startRecognizing()
            }) {
                Text("Start Recognizing")
            }
            Button(onClick = {
                mainViewModel.speechRecognizerUtils.stopRecognizer()
            }) {
                Text("Stop Recognizing")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.speechRecognizerUtils.onDestroy()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                permission.RECORD_AUDIO,
                permission.INTERNET,
                permission.READ_EXTERNAL_STORAGE
            ),
            1
        )
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SpeechRecognizerTheme {
        Greeting("Android")
    }
}