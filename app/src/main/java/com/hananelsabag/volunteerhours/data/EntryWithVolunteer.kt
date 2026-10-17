package com.hananelsabag.volunteerhours.data

data class EntryWithVolunteer(
    val id: Long,
    val volunteerId: Long,
    val volunteerName: String,
    val date: String,
    val hours: Float
)
