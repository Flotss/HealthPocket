package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: String,
    val email: String,
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "birthDate") val birthDate: String? = null,
    val gender: String? = null,
    @Json(name = "bloodType") val bloodType: String? = null,
    val allergies: List<String> = emptyList(),
    @Json(name = "emergencyContactName") val emergencyContactName: String? = null,
    @Json(name = "emergencyContactPhone") val emergencyContactPhone: String? = null,
    @Json(name = "preferredLanguage") val preferredLanguage: String = "fr",
    @Json(name = "darkModeEnabled") val darkModeEnabled: Boolean = false
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "birthDate") val birthDate: String? = null,
    val gender: String? = null,
    @Json(name = "bloodType") val bloodType: String? = null,
    val allergies: List<String>? = null,
    @Json(name = "emergencyContactName") val emergencyContactName: String? = null,
    @Json(name = "emergencyContactPhone") val emergencyContactPhone: String? = null,
    @Json(name = "preferredLanguage") val preferredLanguage: String? = null,
    @Json(name = "darkModeEnabled") val darkModeEnabled: Boolean? = null
)

