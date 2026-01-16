package com.healthpocket.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.healthpocket.data.local.HealthPocketDatabase
import com.healthpocket.data.local.dao.AppointmentDao
import com.healthpocket.data.local.dao.HealthLogDao
import com.healthpocket.data.local.dao.MedicationDao
import com.healthpocket.data.local.dao.MedicationIntakeDao
import com.healthpocket.data.local.dao.UserDao
import com.healthpocket.data.local.dao.VitalMetricDao
import com.healthpocket.data.preferences.UserPreferences
import com.healthpocket.data.remote.ApiConfig
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.interceptor.AuthInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Test module that replaces production dependencies with test-friendly versions.
 * - Database: Uses in-memory database instead of persistent database
 * - API: Uses mock API to avoid real network calls
 * - Preferences: Uses in-memory DataStore
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestDatabaseModule {

    // Database - In-memory for tests
    @Provides
    @Singleton
    fun provideTestDatabase(@ApplicationContext context: Context): HealthPocketDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            HealthPocketDatabase::class.java
        )
            .allowMainThreadQueries() // Allow main thread queries in tests
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Database is created in memory, no need to initialize
                }
            })
            .build()
    }

    // DAOs - Provided from test database
    @Provides
    fun provideUserDao(database: HealthPocketDatabase): UserDao = database.userDao()

    @Provides
    fun provideMedicationDao(database: HealthPocketDatabase): MedicationDao = database.medicationDao()

    @Provides
    fun provideMedicationIntakeDao(database: HealthPocketDatabase): MedicationIntakeDao = database.medicationIntakeDao()

    @Provides
    fun provideAppointmentDao(database: HealthPocketDatabase): AppointmentDao = database.appointmentDao()

    @Provides
    fun provideHealthLogDao(database: HealthPocketDatabase): HealthLogDao = database.healthLogDao()

    @Provides
    fun provideVitalMetricDao(database: HealthPocketDatabase): VitalMetricDao = database.vitalMetricDao()

    // Network - Mock API for tests (using mock instead of real network calls)
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE // Disable logging in tests
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        // Use a dummy URL for tests - API calls will be mocked
        return Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideHealthPocketApi(retrofit: Retrofit): HealthPocketApi {
        return TestHealthPocketApi()
    }

    // Preferences - Use in-memory DataStore for tests
    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        // In tests, we can use the real UserPreferences but with test context
        // The test context will use a separate data directory
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(userPreferences: UserPreferences): AuthInterceptor {
        return AuthInterceptor(userPreferences)
    }
}
