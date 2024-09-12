package com.zui.translator.stream

import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * 二进制音频文件读取类
 *
 * 该类用于从指定的文件对应的二进制音频文件中读取数据
 * 它提供了一种方便的方式来处理音频数据的读取操作
 *
 * @param file 音频文件
 */
class BinaryAudioStreamReader internal constructor(file: File) :
    PullAudioInputStreamCallback() {
    private var inputStream: InputStream = FileInputStream(file)

    override fun read(dataBuffer: ByteArray): Int {
        try {
            return inputStream.read(dataBuffer, 0, dataBuffer.size)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * Closes the audio input stream.
     */
    override fun close() {
        try {
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}