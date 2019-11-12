package moe.gogo

import kotlin.random.Random

val DEFAULT_ALPHABET: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun Random.nextString(length: Int = 6, alphabet: List<Char> = DEFAULT_ALPHABET): String =
    CharArray(length) { alphabet.random(this) }.joinToString("")
