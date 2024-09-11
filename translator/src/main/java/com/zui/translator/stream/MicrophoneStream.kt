//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//
package com.zui.translator.stream

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import com.microsoft.cognitiveservices.speech.audio.AudioStreamFormat
import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback

/**
 * MicrophoneStream exposes the Android Microphone as an PullAudioInputStreamCallback
 * to be consumed by the Speech SDK.
 * It configures the microphone with 16 kHz sample rate, 16 bit samples, mono (single-channel).
 */
class MicrophoneStream @RequiresPermission(Manifest.permission.RECORD_AUDIO) constructor() :
    PullAudioInputStreamCallback() {
    val format: AudioStreamFormat =
        AudioStreamFormat.getWaveFormatPCM(SAMPLE_RATE.toLong(), 16.toShort(), 1.toShort())
    private var recorder: AudioRecord? = null

    init {
        this.initMic()
    }

    override fun read(bytes: ByteArray): Int {
        if (this.recorder != null) {
            val ret = recorder?.read(bytes, 0, bytes.size)?.toLong()
            return ret?.toInt() ?: -1
        }
        return 0
    }

    override fun close() {
        recorder?.release()
        this.recorder = null
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun initMic() {
        // Note: currently, the Speech SDK support 16 kHz sample rate, 16 bit samples, mono (single-channel) only.
        val af = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
            .build()

        this.recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            .setAudioFormat(af)
            .build()

        recorder?.startRecording()
    }

    companion object {
        private const val SAMPLE_RATE = 16000

        @RequiresPermission(Manifest.permission.RECORD_AUDIO)
        fun create(): MicrophoneStream {
            return MicrophoneStream()
        }
    }
}