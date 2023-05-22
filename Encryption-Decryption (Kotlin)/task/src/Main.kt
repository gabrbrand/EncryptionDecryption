package encryptdecrypt

import java.io.File

fun main(args: Array<String>) {
    val modeIndex = args.indexOf("-mode")
    val mode = if (modeIndex == -1) Mode.Encrypt else when (args[modeIndex + 1]) {
        Mode.Encrypt.short -> Mode.Encrypt
        Mode.Decrypt.short -> Mode.Decrypt
        else -> Mode.Unsupported
    }

    val keyIndex = args.indexOf("-key")
    val key = if (keyIndex == -1) 0 else args[keyIndex + 1].toInt()

    val dataIndex = args.indexOf("-data")
    val data = if (dataIndex == -1) "" else args[dataIndex + 1]

    val fileInIndex = args.indexOf("-in")
    val fileInName = if (fileInIndex == -1) "" else args[fileInIndex + 1]

    val fileOutIndex = args.indexOf("-out")
    val fileOutName = if (fileOutIndex == -1) "" else args[fileOutIndex + 1]

    val algIndex = args.indexOf("-alg")
    val alg = if (algIndex == -1) Algorithm.Shift else when (args[algIndex + 1]) {
        Algorithm.Shift.name.lowercase() -> Algorithm.Shift
        Algorithm.Unicode.name.lowercase() -> Algorithm.Unicode
        else -> Algorithm.Unsupported
    }

    var input = ""
    when {
        data.isNotEmpty() -> input = data
        fileInName.isNotEmpty() -> {
            val fileIn = File(fileInName)
            if (fileIn.exists()) {
                input = fileIn.readText()
            } else {
                println("Error: File ${fileIn.name} not found!")
            }
        }
    }

    val result = when (mode) {
        Mode.Encrypt -> input.encrypt(key, alg)
        Mode.Decrypt -> input.decrypt(key, alg)
        else -> ""
    }

    if (fileOutName.isNotEmpty()) {
        val fileOut = File(fileOutName)
        fileOut.writeText(result)
    } else {
        println(result)
    }
}

enum class Mode(val short: String = "") {
    Encrypt(short = "enc"),
    Decrypt(short = "dec"),
    Unsupported
}

enum class Algorithm {
    Shift, Unicode, Unsupported
}

fun String.encrypt(key: Int, algorithm: Algorithm): String {
    val originalMessage = this
    val ciphertextBuilder = StringBuilder()

    originalMessage.forEach { letter ->
        ciphertextBuilder.append(
            letter.shift(Mode.Encrypt, key, algorithm)
        )
    }

    return ciphertextBuilder.toString()
}

fun String.decrypt(key: Int, algorithm: Algorithm): String {
    val ciphertext = this
    val originalMessageBuilder = StringBuilder()

    ciphertext.forEach { letter ->
        originalMessageBuilder.append(
            letter.shift(Mode.Decrypt, key, algorithm)
        )
    }

    return originalMessageBuilder.toString()
}

fun Char.shift(mode: Mode, key: Int, algorithm: Algorithm): Char {
    val letter = this
    return when (mode) {
        Mode.Encrypt -> when (algorithm) {
            Algorithm.Shift -> {
                if (letter.isLetter()) {
                    if (letter + key <= 'z') {
                        val shiftedLetter = letter + key
                        if (letter.isUpperCase()) shiftedLetter.uppercaseChar() else shiftedLetter
                    } else {
                        val shiftedLetter = letter + key - 26
                        if (letter.isUpperCase()) shiftedLetter.uppercaseChar() else shiftedLetter
                    }
                } else {
                    letter
                }
            }

            Algorithm.Unicode -> letter + key
            Algorithm.Unsupported -> error("Unsupported algorithm!")
        }

        Mode.Decrypt -> when (algorithm) {
            Algorithm.Shift -> {
                if (letter.isLetter()) {
                    if (letter - key >= 'a') {
                        val shiftedLetter = letter - key
                        if (letter.isUpperCase()) shiftedLetter.uppercaseChar() else shiftedLetter
                    } else {
                        val shiftedLetter = letter - key + 26
                        if (letter.isUpperCase()) shiftedLetter.uppercaseChar() else shiftedLetter
                    }
                } else {
                    letter
                }
            }

            Algorithm.Unicode -> letter - key
            Algorithm.Unsupported -> error("Unsupported algorithm!")
        }

        Mode.Unsupported -> error("Unsupported mode!")
    }
}