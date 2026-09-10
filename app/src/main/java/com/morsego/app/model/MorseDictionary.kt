package com.morsego.app.model

data class MorseItem(
    val character: String,
    val morse: String,
    val phonetic: String,
    val category: MorseCategory,
    val tip: String = ""
)

enum class MorseCategory(val displayName: String) {
    LETTERS("Letras"),
    NUMBERS("Números"),
    PUNCTUATION("Pontuação"),
    PROSIGNS("Sinais Especiais (Prosigns)")
}

object MorseDictionary {

    val ITEMS = listOf(
        // Letters A-Z
        MorseItem("A", ".-", "Alfa", MorseCategory.LETTERS, "di-DAH"),
        MorseItem("B", "-...", "Bravo", MorseCategory.LETTERS, "DAH-di-di-dit"),
        MorseItem("C", "-.-.", "Charlie", MorseCategory.LETTERS, "DAH-di-DAH-dit"),
        MorseItem("D", "-..", "Delta", MorseCategory.LETTERS, "DAH-di-dit"),
        MorseItem("E", ".", "Echo", MorseCategory.LETTERS, "dit"),
        MorseItem("F", "..-.", "Foxtrot", MorseCategory.LETTERS, "di-di-DAH-dit"),
        MorseItem("G", "--.", "Golf", MorseCategory.LETTERS, "DAH-DAH-dit"),
        MorseItem("H", "....", "Hotel", MorseCategory.LETTERS, "di-di-di-dit"),
        MorseItem("I", "..", "India", MorseCategory.LETTERS, "di-dit"),
        MorseItem("J", ".---", "Juliett", MorseCategory.LETTERS, "di-DAH-DAH-DAH"),
        MorseItem("K", "-.-", "Kilo", MorseCategory.LETTERS, "DAH-di-DAH"),
        MorseItem("L", ".-..", "Lima", MorseCategory.LETTERS, "di-DAH-di-dit"),
        MorseItem("M", "--", "Mike", MorseCategory.LETTERS, "DAH-DAH"),
        MorseItem("N", "-.", "November", MorseCategory.LETTERS, "DAH-dit"),
        MorseItem("O", "---", "Oscar", MorseCategory.LETTERS, "DAH-DAH-DAH"),
        MorseItem("P", ".--.", "Papa", MorseCategory.LETTERS, "di-DAH-DAH-dit"),
        MorseItem("Q", "--.-", "Quebec", MorseCategory.LETTERS, "DAH-DAH-di-DAH"),
        MorseItem("R", ".-.", "Romeo", MorseCategory.LETTERS, "di-DAH-dit"),
        MorseItem("S", "...", "Sierra", MorseCategory.LETTERS, "di-di-dit"),
        MorseItem("T", "-", "Tango", MorseCategory.LETTERS, "DAH"),
        MorseItem("U", "..-", "Uniform", MorseCategory.LETTERS, "di-di-DAH"),
        MorseItem("V", "...-", "Victor", MorseCategory.LETTERS, "di-di-di-DAH"),
        MorseItem("W", ".--", "Whiskey", MorseCategory.LETTERS, "di-DAH-DAH"),
        MorseItem("X", "-..-", "X-ray", MorseCategory.LETTERS, "DAH-di-di-DAH"),
        MorseItem("Y", "-.--", "Yankee", MorseCategory.LETTERS, "DAH-di-DAH-DAH"),
        MorseItem("Z", "--..", "Zulu", MorseCategory.LETTERS, "DAH-DAH-di-dit"),

        // Numbers 0-9
        MorseItem("0", "-----", "Zero", MorseCategory.NUMBERS),
        MorseItem("1", ".----", "Um", MorseCategory.NUMBERS),
        MorseItem("2", "..---", "Dois", MorseCategory.NUMBERS),
        MorseItem("3", "...--", "Três", MorseCategory.NUMBERS),
        MorseItem("4", "....-", "Quatro", MorseCategory.NUMBERS),
        MorseItem("5", ".....", "Cinco", MorseCategory.NUMBERS),
        MorseItem("6", "-....", "Seis", MorseCategory.NUMBERS),
        MorseItem("7", "--...", "Sete", MorseCategory.NUMBERS),
        MorseItem("8", "---..", "Oito", MorseCategory.NUMBERS),
        MorseItem("9", "----.", "Nove", MorseCategory.NUMBERS),

        // Punctuation
        MorseItem(".", ".-.-.-", "Ponto final", MorseCategory.PUNCTUATION),
        MorseItem(",", "--..--", "Vírgula", MorseCategory.PUNCTUATION),
        MorseItem("?", "..--..", "Interrogação", MorseCategory.PUNCTUATION),
        MorseItem("/", "-..-.", "Barra oblíqua", MorseCategory.PUNCTUATION),
        MorseItem("=", "-...-", "Igual / Novo parágrafo", MorseCategory.PUNCTUATION),
        MorseItem("-", "-....-", "Hífen / Traço", MorseCategory.PUNCTUATION),
        MorseItem("@", ".--.-.", "Arroba (AC)", MorseCategory.PUNCTUATION),
        MorseItem("!", "-.-.--", "Exclamação", MorseCategory.PUNCTUATION),

        // Prosigns
        MorseItem("SOS", "...---...", "Socorro / Emergência", MorseCategory.PROSIGNS, "Chamada de perigo internacional"),
        MorseItem("AR", ".-.-.", "Fim de mensagem (+)", MorseCategory.PROSIGNS, "Transmissão concluída"),
        MorseItem("SK", "...-.-", "Fim de contacto", MorseCategory.PROSIGNS, "Silent Key / Desconexão"),
        MorseItem("BT", "-...-", "Separador (=)", MorseCategory.PROSIGNS, "Pausa ou quebra de texto"),
        MorseItem("AS", ".-...", "Aguarde", MorseCategory.PROSIGNS, "Espere um momento"),
        MorseItem("KN", "-.--.", "Convite específico", MorseCategory.PROSIGNS, "Apenas a estação chamada deve responder")
    )

    private val charToMorse = ITEMS.associate { it.character.uppercase() to it.morse }
    private val morseToChar = ITEMS.associate { it.morse to it.character.uppercase() }

    fun getMorse(char: Char): String? = charToMorse[char.uppercaseChar().toString()]

    fun getMorse(str: String): String? = charToMorse[str.uppercase()]

    fun getChar(morse: String): String? = morseToChar[morse]

    fun textToMorse(text: String): String {
        return text.uppercase().split(" ").joinToString(" / ") { word ->
            word.mapNotNull { getMorse(it) }.joinToString(" ")
        }
    }

    fun morseToText(morse: String): String {
        return morse.trim().split(" / ").joinToString(" ") { word ->
            word.trim().split(" ").mapNotNull { getChar(it) }.joinToString("")
        }
    }
}
