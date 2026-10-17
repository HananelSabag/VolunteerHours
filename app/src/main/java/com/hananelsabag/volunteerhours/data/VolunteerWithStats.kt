package com.hananelsabag.volunteerhours.data

data class VolunteerWithStats(
    val id: Long,
    val name: String,
    val phone: String,
    val createdAt: Long,
    val totalHours: Float,
    val entryCount: Int,
    val lastActivity: String?
)
