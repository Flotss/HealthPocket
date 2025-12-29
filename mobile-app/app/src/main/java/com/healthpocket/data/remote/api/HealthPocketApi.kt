package com.healthpocket.data.remote.api

import com.healthpocket.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for HealthPocket backend.
 */
interface HealthPocketApi {

    // Authentication
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    // User
    @GET("api/users/profile")
    suspend fun getProfile(): Response<UserResponse>

    @PUT("api/users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    // Medications
    @GET("api/medications")
    suspend fun getAllMedications(): Response<List<MedicationResponse>>

    @GET("api/medications/active")
    suspend fun getActiveMedications(): Response<List<MedicationResponse>>

    @GET("api/medications/{id}")
    suspend fun getMedicationById(@Path("id") id: String): Response<MedicationResponse>

    @POST("api/medications")
    suspend fun createMedication(@Body request: MedicationRequest): Response<MedicationResponse>

    @PUT("api/medications/{id}")
    suspend fun updateMedication(@Path("id") id: String, @Body request: MedicationRequest): Response<MedicationResponse>

    @DELETE("api/medications/{id}")
    suspend fun deleteMedication(@Path("id") id: String): Response<Unit>

    // Medication Intakes
    @GET("api/medications/{medicationId}/intakes")
    suspend fun getIntakesForMedication(@Path("medicationId") medicationId: String): Response<List<MedicationIntakeResponse>>

    @GET("api/medications/intakes")
    suspend fun getIntakesInRange(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<List<MedicationIntakeResponse>>

    @POST("api/medications/intakes")
    suspend fun createIntake(@Body request: MedicationIntakeRequest): Response<MedicationIntakeResponse>

    @PUT("api/medications/intakes/{id}/status")
    suspend fun updateIntakeStatus(
        @Path("id") id: String,
        @Body request: UpdateIntakeStatusRequest
    ): Response<MedicationIntakeResponse>

    // Appointments
    @GET("api/appointments")
    suspend fun getAllAppointments(): Response<List<AppointmentResponse>>

    @GET("api/appointments/upcoming")
    suspend fun getUpcomingAppointments(): Response<List<AppointmentResponse>>

    @GET("api/appointments/{id}")
    suspend fun getAppointmentById(@Path("id") id: String): Response<AppointmentResponse>

    @POST("api/appointments")
    suspend fun createAppointment(@Body request: AppointmentRequest): Response<AppointmentResponse>

    @PUT("api/appointments/{id}")
    suspend fun updateAppointment(@Path("id") id: String, @Body request: AppointmentRequest): Response<AppointmentResponse>

    @DELETE("api/appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: String): Response<Unit>

    // Health Logs
    @GET("api/health-logs")
    suspend fun getAllHealthLogs(): Response<List<HealthLogResponse>>

    @GET("api/health-logs/date/{date}")
    suspend fun getHealthLogByDate(@Path("date") date: String): Response<HealthLogResponse>

    @POST("api/health-logs")
    suspend fun createOrUpdateHealthLog(@Body request: HealthLogRequest): Response<HealthLogResponse>

    @DELETE("api/health-logs/{id}")
    suspend fun deleteHealthLog(@Path("id") id: String): Response<Unit>

    // Vital Metrics
    @GET("api/vitals")
    suspend fun getAllVitalMetrics(): Response<List<VitalMetricResponse>>

    @GET("api/vitals/summary")
    suspend fun getVitalMetricsSummary(): Response<VitalMetricsSummary>

    @GET("api/vitals/type/{type}")
    suspend fun getVitalMetricsByType(@Path("type") type: String): Response<List<VitalMetricResponse>>

    @POST("api/vitals")
    suspend fun createVitalMetric(@Body request: VitalMetricRequest): Response<VitalMetricResponse>

    @DELETE("api/vitals/{id}")
    suspend fun deleteVitalMetric(@Path("id") id: String): Response<Unit>

    // Sync
    @POST("api/sync")
    suspend fun sync(@Body request: SyncRequest): Response<SyncResponse>
}

