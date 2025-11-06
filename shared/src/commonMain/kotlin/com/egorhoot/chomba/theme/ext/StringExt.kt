package com.egorhoot.chomba.theme.ext


private const val MIN_PASS_LENGTH = 6
private const val PASS_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{4,}$"

//fun String.isValidEmail(): Boolean {
//    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
//}
//
//fun String.isValidPassword(): Boolean {
//    return this.isNotBlank() &&
//            this.length >= MIN_PASS_LENGTH &&
//            Pattern.compile(PASS_PATTERN).matcher(this).matches()
//}
//
//fun String.passwordMatches(repeated: String): Boolean {
//    return this == repeated
//}
//
//fun String.parseTime(): Date? {
//    if (this.isEmpty()) return null
//    val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
//    utcFormat.timeZone = TimeZone.getTimeZone("UTC")
//
//    return utcFormat.parse(this)
//}