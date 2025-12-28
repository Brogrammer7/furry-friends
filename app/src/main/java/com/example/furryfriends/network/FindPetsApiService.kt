package com.example.furryfriends.network

import com.example.furryfriends.BuildConfig
import com.example.furryfriends.model.Pets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://api.rescuegroups.org/v5/public/"
private const val rescueGroupsApiKey = BuildConfig.API_KEY

private class ApiKeyInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("Content-Type", "application/vnd.api+json")
            .header("Authorization", rescueGroupsApiKey)
            .build()
        return chain.proceed(request)
    }
}

private val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private val client = OkHttpClient.Builder()
    .addInterceptor(ApiKeyInterceptor())
    .addInterceptor(logging)
    .build()

private val retrofit: Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .baseUrl(BASE_URL)
    .build()

interface FindPetsApiService {
    @GET("animals")
    suspend fun getAvailablePets(
        @Query("limit") limit: Int = 20,
//        @Query("page") pageNumber: Int = 1
    ): Pets


//    @POST("public/animals/search/available/haspic")
//    suspend fun getAnimals(
//        @Query("fields[cats]") fields: Int = 25,
//        @Query("postalCode") postalCode: String = "90028",
//        @Query("limit") limit: Int = 1
//    ):


}

object PetsApi {
    val retrofitService: FindPetsApiService by lazy {
        retrofit.create(FindPetsApiService::class.java)
    }
}