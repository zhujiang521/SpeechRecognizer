package com.zui.translator.network

import com.google.gson.GsonBuilder
import com.zui.translator.model.TranslatorModel
import com.zui.translator.utils.LANGUAGE_CHINESE
import com.zui.translator.utils.LANGUAGE_ENGLISH
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 用于翻译文本的服务类，使用 Azure Translator API。
 *
 * 下面的链接是所有language的缩写，在获取翻译时需要
 * https://learn.microsoft.com/en-us/azure/ai-services/speech-service/language-support?tabs=stt
 */
class TranslatorText {

    companion object {
        private const val BASE_URL = "https://api.translator.azure.cn/"
    }

    private var translatorService: TranslatorService
    private var translateUrl = "translate?api-version=3.0"

    init {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        translatorService = retrofit.create(TranslatorService::class.java)
    }

    /**
     * 异步翻译文本
     *
     * 该函数提供了一个异步的翻译接口，允许指定源文本和目标语言，默认源语言为英文
     *
     * @param text 待翻译的文本
     * @param from 源语言代码，默认为"en"表示英文
     * @param toTranslation 目标语言代码，表示翻译的目标语言
     * @return 返回一个[Result]对象，封装了翻译后的[TranslatorModel]对象
     *         包含翻译结果和其他相关信息
     *
     * 该函数内部调用了[translateMore]函数来执行实际的翻译工作
     */
    suspend fun translate(
        text: String,
        from: String = LANGUAGE_ENGLISH,
        toTranslation: String = LANGUAGE_CHINESE
    ): Result<TranslatorModel> {
        return translate(text, from, toTranslations = arrayOf(toTranslation))
    }

    /**
     * 异步翻译功能，将给定文本从源语言翻译成一种或多种目标语言。
     *
     * @param text 要翻译的文本。
     * @param from 文本的源语言，默认为 "en"。
     * @param toTranslation 一个或多个目标语言的可变参数。
     * @return 返回一个 Result 对象，其中包含 TranslatorModel 或翻译失败的异常信息。
     */
    suspend fun translate(
        text: String,
        from: String = LANGUAGE_ENGLISH,
        vararg toTranslations: String
    ): Result<TranslatorModel> {
        translateUrl = "${translateUrl}&from=${from}"
        toTranslations.forEach {
            translateUrl = "${translateUrl}&to=${it}"
        }
        return try {
            val mediaType: MediaType? = "application/json".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, "[{\"Text\": \"$text\"}]")
            val response = translatorService.translate(translateUrl, body)
            val translatorModel = response.body()
            if (response.isSuccessful && translatorModel != null) {
                Result.success(translatorModel)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Translation failed: ${response.code()} $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
