package net.axay.pacmc.utils

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor
import java.util.*

val dateTimeFormatter: DateTimeFormatter by lazy {
    DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.getDefault())
}

fun TemporalAccessor.formatShort(): String = dateTimeFormatter.format(this)
