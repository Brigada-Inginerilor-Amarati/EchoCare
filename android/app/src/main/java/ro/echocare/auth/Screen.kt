package ro.echocare.auth

/** What the sign-in flow shows (auth-layer §8.2). [error] is a string resource id. */
sealed interface Screen {
    data class Phone(val error: Int? = null) : Screen

    data class Code(val error: Int? = null) : Screen

    data object Loading : Screen

    /** Signed in. What the person may do is not looked up yet. */
    data object SignedIn : Screen
}

/** What the person typed, as E.164 (auth-layer §7.1). The server normalizes again. */
fun toE164(raw: String): String {
    val digits = raw.replace(Regex("[\\s\\-.()]"), "")
    return when {
        digits.startsWith("00") -> "+" + digits.drop(2)
        digits.startsWith("0") -> "+40" + digits.drop(1)
        digits.startsWith("7") -> "+40$digits"
        else -> digits
    }
}
