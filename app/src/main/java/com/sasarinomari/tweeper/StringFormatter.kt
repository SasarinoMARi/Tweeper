package com.sasarinomari.tweeper

class StringFormatter {
    companion object {
        fun extractionString(string: String, startKeyword: String?, endKeyword: String?): String? {
            var startIndex = 0
            if (startKeyword != null) startIndex = string.indexOf(startKeyword) + startKeyword.length
            var endIndex = string.length
            if (endKeyword != null) endIndex = string.indexOf(endKeyword, startIndex)
            if (endIndex == -1) return null // 트위터 인증 거부한 경우
            return string.substring(startIndex, endIndex)
        }
    }
}