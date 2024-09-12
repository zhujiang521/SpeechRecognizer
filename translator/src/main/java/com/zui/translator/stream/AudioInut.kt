package com.zui.translator.stream

import androidx.annotation.IntDef

/**
 * 音频输入类型的注解，用于限定音频输入的类型
 * 该注解使用 IntDef 来定义一组特定的整数值，这些值代表不同的音频输入类型
 */
@IntDef(
    value = [
        AUDIO_TYPE_MICROPHONE, // 麦克风输入
        AUDIO_TYPE_SYSTEM,      // 系统音频输入
        AUDIO_TYPE_FILE         // 文件音频输入
    ],
)
annotation class AudioInputValue

// 麦克风音频输入类型标识符
const val AUDIO_TYPE_MICROPHONE = 10

// 系统音频输入类型标识符
const val AUDIO_TYPE_SYSTEM = 20

// 文件音频输入类型标识符
const val AUDIO_TYPE_FILE = 30