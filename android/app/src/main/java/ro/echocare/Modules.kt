// shortcut: one module so far, and ktlint names such a file after it; the Firebase module joins it next.
@file:Suppress("ktlint:standard:filename")

package ro.echocare

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // ponytail: Retrofit is built here, not provided, until a second API needs it.
    @Provides
    @Singleton
    fun echoCareApi(): EchoCareApi = Retrofit.Builder()
        // Every FastAPI route is under /api; API_URL is the bare origin. Retrofit requires the trailing slash.
        .baseUrl(BuildConfig.API_URL + "/api/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()
}
