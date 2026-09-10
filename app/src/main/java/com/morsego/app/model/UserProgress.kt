package com.morsego.app.model

data class UserProgress(
    val currentKochLevel: Int = 1,
    val completedLevels: Set<Int> = emptySet(),
    val totalCharactersKeyed: Int = 0,
    val correctKeyedCount: Int = 0,
    val correctDecodedCount: Int = 0,
    val bestWpm: Int = 15
) {
    val keyingAccuracy: Float
        get() = if (totalCharactersKeyed > 0) {
            (correctKeyedCount.toFloat() / totalCharactersKeyed) * 100f
        } else 0f
}
