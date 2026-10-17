package com.hananelsabag.volunteerhours.ui

import java.text.SimpleDateFormat
import java.util.*

fun formatDate(iso: String): String = try {
    val from = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val to   = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    to.format(from.parse(iso)!!)
} catch (_: Exception) { iso }

fun formatHours(hours: Float): String =
    if (hours == hours.toLong().toFloat()) hours.toLong().toString() else "%.1f".format(hours)
