package com.zj.speech

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zui.translator.utils.LANGUAGE_CHINESE
import com.zui.translator.utils.LANGUAGE_ENGLISH
import com.zui.translator.SpeechRecognizerUtils
import com.zui.translator.model.SpeechModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val speechFlow: Flow<SpeechModel>
        get() = _speechFlow
    private val _speechFlow by lazy { MutableSharedFlow<SpeechModel>() }
    @SuppressLint("MissingPermission")
    private val speechRecognizerUtils = SpeechRecognizerUtils(LANGUAGE_CHINESE)

    fun startRecognizing() {
        viewModelScope.launch {
            speechRecognizerUtils.startRecognizing(LANGUAGE_ENGLISH).collect {
                _speechFlow.emit(it)
            }
        }
    }

}