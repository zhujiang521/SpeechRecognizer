package com.zui.translator.model

import com.microsoft.cognitiveservices.speech.SpeechRecognitionEventArgs

/**
 * 表示语音模型，封装了与语音事件相关的信息。
 * 此数据类用于传输语音识别或翻译事件的详细信息。
 *
 * @param sender 触发语音事件的对象，可以是任意类型
 * @param event 语音识别事件参数，可能为 null
 * @param isFinal 是否为最终结果，可能为 null
 * @param translatorModel 翻译模型，可能为 null
 */

data class SpeechModel(
    val sender: Any? = null,
    val event: SpeechRecognitionEventArgs? = null,
    val isFinal: Boolean? = null,
    val translatorModel: TranslatorModel? = null
)
