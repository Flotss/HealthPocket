package com.healthpocket.di

import android.content.Context
import androidx.room.Room
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
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module providing application-wide dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Database
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HealthPocketDatabase {
        return Room.databaseBuilder(
            context,
            HealthPocketDatabase::class.java,
            HealthPocketDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    @Provides
    fun provideUserDao(database: HealthPocketDatabase): UserDao = database.userDao()

    @Provides
    fun provideMedicationDao(database: HealthPocketDatabase): MedicationDao =
        database.medicationDao()

    @Provides
    fun provideMedicationIntakeDao(database: HealthPocketDatabase): MedicationIntakeDao =
        database.medicationIntakeDao()

    @Provides
    fun provideAppointmentDao(database: HealthPocketDatabase): AppointmentDao =
        database.appointmentDao()

    @Provides
    fun provideHealthLogDao(database: HealthPocketDatabase): HealthLogDao = database.healthLogDao()

    @Provides
    fun provideVitalMetricDao(database: HealthPocketDatabase): VitalMetricDao =
        database.vitalMetricDao()

    // Network
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideHealthPocketApi(retrofit: Retrofit): HealthPocketApi {
        return retrofit.create(HealthPocketApi::class.java)
    }

    // Preferences
    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }
}

