package ro.echocare.home

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import ro.echocare.SeniorPage

// What the consent text and the dates under it need (UC-01, UC-04). Plain Kotlin, tested on the JVM.

internal fun instantOf(text: String?): Instant? =
    text?.let { runCatching { Instant.parse(it) }.getOrNull() }

// The consent text and its signature belong before the welcome call: until acceptance, signed
// or not, so a saved signature shows „Semnat” in its place (G7, UC-01 step 5, UC-04).
fun showsConsent(page: SeniorPage): Boolean = page.state in setOf("draft", "submitted", "accepted")

private val romanian = Locale.forLanguageTag("ro")
private val dayMonth = DateTimeFormatter.ofPattern("d MMM", romanian)
private val dayMonthYear = DateTimeFormatter.ofPattern("d MMM yyyy", romanian)

// A stage's date, "4 nov."; with [year], "4 nov. 2026". An unreadable date shows as sent.
fun shortDate(iso: String, zone: ZoneId, year: Boolean = false): String =
    instantOf(iso)?.atZone(zone)?.let { (if (year) dayMonthYear else dayMonth).format(it) } ?: iso
