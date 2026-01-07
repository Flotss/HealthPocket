package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: String,
    val email: String,
    @field:Json(name = "firstName") val firstName: String,
    @field:Json(name = "lastName") val lastName: String,
    @field:Json(name = "birthDate") val birthDate: String? = null,
    val gender: String? = null,
    @field:Json(name = "bloodType") val bloodType: String? = null,
    val allergies: List<String> = emptyList(),
    @field:Json(name = "emergencyContactName") val emergencyContactName: String? = null,
    @field:Json(name = "emergencyContactPhone") val emergencyContactPhone: String? = null,
    @field:Json(name = "preferredLanguage") val preferredLanguage: String = "fr",
    @field:Json(name = "darkModeEnabled") val darkModeEnabled: Boolean = false
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @field:Json(name = "firstName") val firstName: String,
    @field:Json(name = "lastName") val lastName: String,
    @field:Json(name = "birthDate") val birthDate: String? = null,
    val gender: String? = null,
    @field:Json(name = "bloodType") val bloodType: String? = null,
    val allergies: List<String>? = null,
    @field:Json(name = "emergencyContactName") val emergencyContactName: String? = null,
    @field:Json(name = "emergencyContactPhone") val emergencyContactPhone: String? = null,
    @field:Json(name = "preferredLanguage") val preferredLanguage: String? = null,
    @field:Json(name = "darkModeEnabled") val darkModeEnabled: Boolean? = null
)

