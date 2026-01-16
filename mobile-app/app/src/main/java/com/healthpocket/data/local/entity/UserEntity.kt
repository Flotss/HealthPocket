package com.healthpocket.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Room entity for User profile.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,

    val email: String,

    @ColumnInfo(name = "first_name")
    val firstName: String,

    @ColumnInfo(name = "last_name")
    val lastName: String,

    @ColumnInfo(name = "birth_date")
    val birthDate: LocalDate? = null,

    val gender: String? = null,

    @ColumnInfo(name = "blood_type")
    val bloodType: String? = null,

    val allergies: String? = null, // JSON array

    @ColumnInfo(name = "emergency_contact_name")
    val emergencyContactName: String? = null,

    @ColumnInfo(name = "emergency_contact_phone")
    val emergencyContactPhone: String? = null,

    @ColumnInfo(name = "preferred_language")
    val preferredLanguage: String = "fr",

    @ColumnInfo(name = "dark_mode_enabled")
    val darkModeEnabled: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

