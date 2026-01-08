package com.example.furryfriends.network

import com.example.furryfriends.BuildConfig
import com.example.furryfriends.model.FindResponse
import com.example.furryfriends.model.SearchRequest
import com.example.furryfriends.model.SearchResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://api.rescuegroups.org/v5/public/"
private const val rescueGroupsApiKey = BuildConfig.API_KEY

enum class Species(val type: String) {
    CATS("cats"),
    DOGS("dogs"),
    RABBITS("rabbits"),
    TURTLES("turtles")
}

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

interface FurryFriendsApiService {
    //Simple search of available pets with no filtration
    @GET("animals/search")
    suspend fun getAvailablePets(
        @Query("limit") limit: Int = 30,
        @Query("page") pageNumber: Int = 1,
        @Query("type") type: String = "Cat",
        @Query("location") zip: String = "92692",
        @Query("radius") radiusMiles: Int = 25,
    ): FindResponse

    //Advanced search by animal type and location
    @POST("animals/search/available/{species}/haspic/")
    suspend fun searchPets(
        @Path("species") species: String,
        @Query("sort") sort: String = "random",
        @Query("limit") limit: Int = 30,
        @Query("include") include: String = "pictures,orgs",
        @Body body: SearchRequest
    ): SearchResponse

}

object PetsApi {
    val retrofitService: FurryFriendsApiService by lazy {
        retrofit.create(FurryFriendsApiService::class.java)
    }
}