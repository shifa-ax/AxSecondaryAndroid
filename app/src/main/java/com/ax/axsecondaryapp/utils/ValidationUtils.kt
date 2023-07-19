package com.ax.axsecondaryapp.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

object ValidationUtils {
    fun validateEmailAddress(emailAddress: String?): Boolean {
        val regexPattern =
            Pattern.compile("^[(a-zA-Z-0-9-\\_\\+\\.)]+@[(a-z-A-z)]+\\.[(a-zA-z)]{2,3}$")
        val regMatcher = regexPattern.matcher(emailAddress)
        return regMatcher.matches()
    }

    fun validateMobileNumber(mobileNumber: String?): Boolean {
        val regexPattern = Pattern.compile("^[-+]?\\d+$")
        val regMatcher = regexPattern.matcher(mobileNumber)
        return regMatcher.matches()
    }

    fun validatePassword(password: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z-a-z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }
}