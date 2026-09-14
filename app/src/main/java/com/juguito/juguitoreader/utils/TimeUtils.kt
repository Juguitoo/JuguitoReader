package com.juguito.juguitoreader.utils

import java.time.LocalDate
import java.time.ZoneOffset

fun localTodayUtcMidnightMillis(): Long =
    LocalDate.now()
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

fun utcMidnightMillis(date: LocalDate?): Long =
    date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() ?: localTodayUtcMidnightMillis()