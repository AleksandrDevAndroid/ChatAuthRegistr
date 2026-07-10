package ru.netology.nmedia.enum

enum class DatePublished(val day: String) {
    TODAY("Сегодня"),
    YESTERDAY("Вчера"),
    WEEK("На прошлой недели");

    companion object {
        fun getTime(published: Long): DatePublished {
            val currentTimeSeconds = System.currentTimeMillis() / 1000L
            val diffSeconds = currentTimeSeconds - published
            val oneDaySeconds = 24 * 60 * 60L
            val twoDaysSeconds = 48 * 60 * 60L

            return when {
                diffSeconds < oneDaySeconds -> DatePublished.TODAY
                diffSeconds < twoDaysSeconds -> DatePublished.YESTERDAY
                else -> DatePublished.WEEK
            }

        }
    }
}

