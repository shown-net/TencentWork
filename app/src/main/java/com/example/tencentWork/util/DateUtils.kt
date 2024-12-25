package com.example.tencentWork.util

import java.time.LocalDateTime
import java.time.ZoneOffset

object DateUtils {

    //过期天数
    val expireDay: Long = 7

    fun localDateTimeToMillis(
        localDateTime: LocalDateTime,
        zoneOffset: ZoneOffset = ZoneOffset.UTC
    ): Long {
        return localDateTime.toInstant(zoneOffset).toEpochMilli()
    }


    fun localDateTimeToSeconds(
        localDateTime: LocalDateTime,
        zoneOffset: ZoneOffset = ZoneOffset.UTC
    ): Long {
        return localDateTime.toInstant(zoneOffset).epochSecond
    }


    fun millisToLocalDateTime(
        millis: Long,
        zoneOffset: ZoneOffset = ZoneOffset.UTC
    ): LocalDateTime {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), zoneOffset)
    }

    fun secondsToLocalDateTime(
        seconds: Long,
        zoneOffset: ZoneOffset = ZoneOffset.UTC
    ): LocalDateTime {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(seconds), zoneOffset)
    }
}
