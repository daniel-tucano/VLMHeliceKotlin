package utils

fun isNormalizedValue (t: Double): Boolean {
    return t in 0.0..1.0
}