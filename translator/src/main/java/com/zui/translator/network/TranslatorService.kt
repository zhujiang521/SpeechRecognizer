package com.zui.translator.network

import com.zui.translator.model.TranslatorModel
import com.zui.translator.utils.TRANSLATOR_API_KEY
import com.zui.translator.utils.TRANSLATOR_LOCATION
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.Url

interface TranslatorService {
    @Headers(
        "Ocp-Apim-Subscription-Key: $TRANSLATOR_API_KEY",
        "Ocp-Apim-Subscription-Region: $TRANSLATOR_LOCATION",
        "Content-Type: application/json"
    )
    @POST
    suspend fun translate(
        @Url url: String,
        @Body body: RequestBody
    ): Response<TranslatorModel>
}
