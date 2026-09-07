package com.myscratch.app.data.network

import com.myscratch.app.data.network.api.MyScratchApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // 127.0.0.1:8000 works seamlessly on real devices via USB with 'adb reverse tcp:8000 tcp:8000'
    var baseUrl: String = "http://127.0.0.1:8000/api/"

    private var apiService: MyScratchApiService? = null

    fun getService(tokenManager: TokenManager): MyScratchApiService {
        if (apiService == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenManager))
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(MyScratchApiService::class.java)
        }
        return apiService!!
    }
}
