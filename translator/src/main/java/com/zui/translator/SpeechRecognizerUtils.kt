package com.zui.translator

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RequiresPermission
import com.microsoft.cognitiveservices.speech.SourceLanguageConfig
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionEventArgs
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback
import com.zui.translator.model.SpeechModel
import com.zui.translator.network.TranslatorText
import com.zui.translator.stream.AUDIO_TYPE_MICROPHONE
import com.zui.translator.stream.AUDIO_TYPE_SYSTEM
import com.zui.translator.stream.AudioInputValue
import com.zui.translator.stream.MicrophoneStream
import com.zui.translator.stream.createAudioInputStream
import com.zui.translator.utils.LANGUAGE_CHINESE
import com.zui.translator.utils.LANGUAGE_ENGLISH
import com.zui.translator.utils.SPEECH_REGION
import com.zui.translator.utils.SPEECH_SUBSCRIPTION_KEY
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * 语音识别工具类
 *
 * 该类构造函数要求设备具有录音权限，以进行语音识别相关操作
 * 下面是对应的语言代码，可以去对应网站进行查询
 * https://learn.microsoft.com/en-us/azure/ai-services/speech-service/language-support?tabs=stt
 */
@SuppressLint("MissingPermission")
class SpeechRecognizerUtils @RequiresPermission(Manifest.permission.RECORD_AUDIO) constructor(
    private val language: String = LANGUAGE_ENGLISH,
    @AudioInputValue private val audioInputType: Int = AUDIO_TYPE_SYSTEM
) {

    companion object {
        private const val TAG = "SpeechRecognizerUtils"
    }

    private val coroutineScope = MainScope()
    private val executorService = Executors.newCachedThreadPool()
    private var speechConfig: SpeechConfig? = null
    private var sourceLanguageConfig: SourceLanguageConfig? = null
    private var inputStream: PullAudioInputStreamCallback? = null
    private var audioConfig: AudioConfig? = null
    private val translateRequest = TranslatorText()
    private val speechFlow: Flow<Triple<Any, SpeechRecognitionEventArgs, Boolean>>
        get() = _speechFlow
    private val _speechFlow by lazy { MutableSharedFlow<Triple<Any, SpeechRecognitionEventArgs, Boolean>>() }

    private var speechRecognizer: SpeechRecognizer? = null


    /**
     * 初始化语音识别器
     */
    init {
        initSpeechRecognizer()
    }

    @SuppressLint("MissingPermission")
    private fun initSpeechRecognizer() {
        speechConfig = SpeechConfig.fromSubscription(SPEECH_SUBSCRIPTION_KEY, SPEECH_REGION)
        sourceLanguageConfig = SourceLanguageConfig.fromLanguage(language)
        destroyMicrophoneStream() // in case it was previously initialized
        inputStream = createAudioInputStream(audioInputType)
        audioConfig = AudioConfig.fromStreamInput(inputStream)
        speechRecognizer = SpeechRecognizer(
            speechConfig,
            sourceLanguageConfig,
            audioConfig
        )
    }

    /**
     * 启动语音识别器
     *
     * 该函数负责启动语音识别器的连续识别功能，并对识别过程中的事件进行监听
     * 使用executorService来确保异步执行语音识别的启动操作
     */
    private fun startRealRecognizing() {
        speechRecognizer?.recognizing?.addEventListener { sender, e ->
            coroutineScope.launch {
                _speechFlow.emit(Triple(sender, e, false))
            }
        }
        speechRecognizer?.recognized?.addEventListener { sender, e ->
            coroutineScope.launch {
                _speechFlow.emit(Triple(sender, e, true))
            }
        }
        val task = speechRecognizer?.startContinuousRecognitionAsync()
        executorService.submit {
            task?.get()
        }
    }

    /**
     * 开始语音识别并翻译成指定语言
     *
     * @param toTranslation 目标翻译语言，默认为LANGUAGE_CHINESE
     * @return Unit
     *
     * 此 suspend 函数通过指定目标语言开始语音识别过程
     * 当不需要指定特定翻译语言时，可以使用此函数的默认参数
     */
    suspend fun startRecognizing(
        toTranslation: String = LANGUAGE_CHINESE
    ) = startRecognizing(toTranslations = arrayOf(toTranslation))


    /**
     * 开始识别语音并进行翻译
     *
     * @param toTranslations 需要翻译成的目标语言列表
     * @return Flow<SpeechModel> 返回包含语音识别和翻译结果的SpeechModel对象流
     */
    suspend fun startRecognizing(
        vararg toTranslations: String
    ) = flow {
        startRealRecognizing()
        speechFlow.collect { startRecognizing ->
            Log.i(TAG, "startRecognized: ${startRecognizing.second.result.text}")
            val response =
                translateRequest.translate(
                    startRecognizing.second.result.text,
                    language,
                    toTranslations = toTranslations
                )
            Log.i(TAG, "startRecognized: response:$response")
            val speechModel = if (response.isSuccess) {
                SpeechModel(
                    startRecognizing.first,
                    startRecognizing.second,
                    startRecognizing.third,
                    response.getOrNull()
                )
            } else {
                SpeechModel(
                    startRecognizing.first,
                    startRecognizing.second,
                    startRecognizing.third,
                    null
                )
            }
            emit(speechModel)
        }
    }

    /**
     * 停止语音识别器的连续识别功能
     *
     * 此方法通过调用语音识别器对象的stopContinuousRecognitionAsync()方法，停止正在进行的连续语音识别操作
     * 当需要结束对连续语音输入的识别时，可以调用此方法
     */
    fun stopRecognizer() {
        speechRecognizer?.stopContinuousRecognitionAsync()
    }

    /**
     * 销毁麦克风流
     *
     * 此方法需要同步访问，因为它涉及到对共享资源（microphoneStream）的修改
     * 在多线程环境中，确保只有一个线程可以执行此操作，以避免数据冲突或损坏
     */
    private fun destroyMicrophoneStream() {
        synchronized(this) {
            inputStream?.close()
        }
    }

    /**
     * 销毁资源
     *
     * 本函数的目的是释放与语音识别相关的资源，包括关闭语音识别器、语音配置、音频配置、源语言配置等
     * 调用本函数通常表示当前不再需要进行语音识别，或者活动/片段即将被销毁，需要清理资源以避免内存泄漏
     */
    fun onDestroy() {
        destroyMicrophoneStream()
        speechRecognizer?.close()
        speechConfig?.close()
        audioConfig?.close()
        sourceLanguageConfig?.close()
        speechConfig = null
    }

}