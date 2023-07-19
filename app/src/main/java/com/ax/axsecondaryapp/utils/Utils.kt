package com.ax.axsecondaryapp.utils

class Utils {
  companion object {
    fun isNumberString(string: String?): Boolean {
      var returnFlag = false
      if (string != null) {
        try {
          val d: Double = string.toDouble()
        } catch (nfe: NumberFormatException) {
          returnFlag = false
        }
        returnFlag = true
      }
      return returnFlag
    }

    fun formatNumberToSimple(string: String): String {
      return string.substringAfter("+").replace(" ", "")
    }

    fun fullNumberToSimple(string: String): String {
      return string.substring(3)
    }


    fun latFromStringComaSeparated(string: String): Double {
      return string.substringBefore(",").toDouble()
    }

    fun LongFromStringComaSeparated(string: String): Double {
      return string.substringAfter(",").toDouble()
    }

  }
}