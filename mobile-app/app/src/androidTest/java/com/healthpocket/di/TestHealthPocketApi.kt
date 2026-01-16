package com.healthpocket.di

import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.*
import retrofit2.Response

class TestHealthPocketApi : HealthPocketApi {
    override suspend fun register(request: RegisterRequest): Response<AuthResponse> = Response.success(null)
    override suspend fun login(request: LoginRequest): Response<AuthResponse> = Response.success(null)
    override suspend fun refreshToken(request: RefreshTokenRequest): Response<TokenResponse> = Response.success(null)
    override suspend fun logout(): Response<Unit> = Response.success(Unit)
    override suspend fun getProfile(): Response<UserResponse> = Response.success(null)
    override suspend fun updateProfile(request: UpdateProfileRequest): Response<UserResponse> = Response.success(null)
    override suspend fun getAllMedications(): Response<List<MedicationResponse>> = Response.success(emptyList())
    override suspend fun getActiveMedications(): Response<List<MedicationResponse>> = Response.success(emptyList())
    override suspend fun getMedicationById(id: String): Response<MedicationResponse> = Response.success(null)
    override suspend fun createMedication(request: MedicationRequest): Response<MedicationResponse> = Response.success(null)
    override suspend fun updateMedication(id: String, request: MedicationRequest): Response<MedicationResponse> = Response.success(null)
    override suspend fun deleteMedication(id: String): Response<Unit> = Response.success(Unit)
    override suspend fun getIntakesForMedication(medicationId: String): Response<List<MedicationIntakeResponse>> = Response.success(emptyList())
    override suspend fun getIntakesInRange(startDate: String, endDate: String): Response<List<MedicationIntakeResponse>> = Response.success(emptyList())
    override suspend fun createIntake(request: MedicationIntakeRequest): Response<MedicationIntakeResponse> = Response.success(null)
    override suspend fun updateIntakeStatus(id: String, request: UpdateIntakeStatusRequest): Response<MedicationIntakeResponse> = Response.success(null)
    override suspend fun getAllAppointments(): Response<List<AppointmentResponse>> = Response.success(emptyList())
    override suspend fun getUpcomingAppointments(): Response<List<AppointmentResponse>> = Response.success(emptyList())
    override suspend fun getAppointmentById(id: String): Response<AppointmentResponse> = Response.success(null)
    override suspend fun createAppointment(request: AppointmentRequest): Response<AppointmentResponse> = Response.success(null)
    override suspend fun updateAppointment(id: String, request: AppointmentRequest): Response<AppointmentResponse> = Response.success(null)
    override suspend fun deleteAppointment(id: String): Response<Unit> = Response.success(Unit)
    override suspend fun getAllHealthLogs(): Response<List<HealthLogResponse>> = Response.success(emptyList())
    override suspend fun getHealthLogByDate(date: String): Response<HealthLogResponse> = Response.success(null)
    override suspend fun createOrUpdateHealthLog(request: HealthLogRequest): Response<HealthLogResponse> = Response.success(null)
    override suspend fun deleteHealthLog(id: String): Response<Unit> = Response.success(Unit)
    override suspend fun getAllVitalMetrics(): Response<List<VitalMetricResponse>> = Response.success(emptyList())
    override suspend fun getVitalMetricsSummary(): Response<VitalMetricsSummary> = Response.success(null)
    override suspend fun getVitalMetricsByType(type: String): Response<List<VitalMetricResponse>> = Response.success(emptyList())
    override suspend fun createVitalMetric(request: VitalMetricRequest): Response<VitalMetricResponse> = Response.success(null)
    override suspend fun deleteVitalMetric(id: String): Response<Unit> = Response.success(Unit)
    override suspend fun sync(request: SyncRequest): Response<SyncResponse> = Response.success(null)
}
