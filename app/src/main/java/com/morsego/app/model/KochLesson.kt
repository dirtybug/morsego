package com.morsego.app.model

data class KochLesson(
    val level: Int,
    val newCharacters: List<String>,
    val allCharacters: List<String>,
    val title: String,
    val description: String
)

object KochMethod {
    // Official standard Koch sequence
    val SEQUENCE = listOf(
        "K", "M", "R", "S", "U", "A", "P", "T", "L", "O",
        "W", "I", ".", "N", "J", "E", "F", "0", "Y", "V",
        ",", "G", "5", "/", "Q", "9", "Z", "H", "3", "8",
        "B", "?", "4", "2", "7", "C", "1", "D", "6", "X"
    )

    fun getLessons(): List<KochLesson> {
        val lessons = mutableListOf<KochLesson>()

        // Lesson 1 introduces K and M
        lessons.add(
            KochLesson(
                level = 1,
                newCharacters = listOf("K", "M"),
                allCharacters = listOf("K", "M"),
                title = "Lição 1: Os Primeiros Passos",
                description = "Aprenda a reconhecer e transmitir o ritmo das letras K (-.-) e M (--)."
            )
        )

        // Lessons 2 to 40 introduce one new character each
        for (i in 2 until SEQUENCE.size) {
            val char = SEQUENCE[i]
            val allChars = SEQUENCE.take(i + 1)
            val morse = MorseDictionary.getMorse(char) ?: ""
            lessons.add(
                KochLesson(
                    level = i,
                    newCharacters = listOf(char),
                    allCharacters = allChars,
                    title = "Lição $i: Nova Letra '$char'",
                    description = "Adicionando '$char' ($morse) ao seu vocabulário CW."
                )
            )
        }

        return lessons
    }

    /**
     * Generates a random sequence of letters from the lesson's available characters.
     */
    fun generatePracticeStream(availableChars: List<String>, count: Int = 10): List<String> {
        return List(count) { availableChars.random() }
    }
}
