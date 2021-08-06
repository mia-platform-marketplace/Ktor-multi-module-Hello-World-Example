package eu.miaplatform.commons.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    inline fun <reified T> build(basePath: String, logLevel: HttpLoggingInterceptor.Level): T {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = logLevel
        }

        val client = OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        val mapper = ObjectMapper().apply {
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerKotlinModule()
        }

        return Retrofit.Builder()
            .baseUrl(basePath)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()
            .create(T::class.java)
    }
}