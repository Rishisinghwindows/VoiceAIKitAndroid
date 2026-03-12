package com.ubudy.voiceaikit.services

import com.ubudy.voiceaikit.models.UserInfo
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.ConcurrentHashMap

internal data class TokenResponse(val token: String, val url: String)

private interface TokenApi {
    @GET("/token")
    suspend fun getToken(
        @Query("name") name: String = "",
        @Query("subject") subject: String = "",
        @Query("grade") grade: String = "",
        @Query("language") language: String = "English",
        @Query("type") type: String = ""
    ): TokenResponse
}

internal object TokenService {
    private val cache = ConcurrentHashMap<String, TokenApi>()

    private fun getApi(baseUrl: String): TokenApi {
        return cache.getOrPut(baseUrl) {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TokenApi::class.java)
        }
    }

    suspend fun fetchToken(serverURL: String, userInfo: UserInfo): TokenResponse {
        return getApi(serverURL).getToken(
            name = userInfo.name,
            subject = userInfo.subject,
            grade = userInfo.grade,
            language = userInfo.language,
            type = userInfo.type
        )
    }
}
