package com.zui.translator.stream

import android.annotation.SuppressLint
import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback

/**
 * 根据音频输入类型创建相应的 PullAudioInputStreamCallback 实现。
 *
 * @param audioInputType 音频输入类型
 * @return 对应的 PullAudioInputStreamCallback 实例
 */
@SuppressLint("MissingPermission")
fun createAudioInputStream(@AudioInputValue audioInputType: Int): PullAudioInputStreamCallback {
    return when (audioInputType) {
        AUDIO_TYPE_MICROPHONE -> MicrophoneStream()
        AUDIO_TYPE_SYSTEM -> SystemAudioStream()
//        AUDIO_TYPE_FILE -> BinaryAudioStreamReader()
        else -> throw IllegalArgumentException("Invalid audio input type: $audioInputType")
    }
}