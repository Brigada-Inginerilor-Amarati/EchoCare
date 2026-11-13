package ro.echocare

import com.google.firebase.auth.FirebaseAuth
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
object FirebaseModule {
    @Provides
    @Singleton
    fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance().apply {
        // Affects only Firebase's test numbers: emulators sign in before the SHA fingerprints
        // are registered (auth-layer §8.2, §9 step 1). Real numbers need verification on, so
        // echocare.realSms=true in local.properties leaves it on in a debug build, which then
        // verifies with the web reCAPTCHA: Play Integrity refused it with 17028 on an emulator.
        if (!BuildConfig.REAL_SMS) {
            firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        } else if (BuildConfig.DEBUG) {
            firebaseAuthSettings.forceRecaptchaFlowForTesting(true)
        }
    }
}

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
