package com.sasarinomari.tweetcleaner

class StringFormatter {
    companion object {
        fun extractionString(string: String, startKeyword: String?, endKeyword: String?): String {
            var startIndex = 0
            if (startKeyword != null) startIndex = string.indexOf(startKeyword) + startKeyword.length
            var endIndex = string.length
            if (endKeyword != null) endIndex = string.indexOf(endKeyword, startIndex)
            return string.substring(startIndex, endIndex)
        }
    }
}