package utils

/**
 * Map any pair of positive values to other pair of positive values where the proportion between value1 and value2
 * are the same but the sum equals complementarySum
 */
fun mapToComplementaryPair(value1: Double, value2: Double, complementarySum: Double = 1.0): Pair<Double, Double> {
    return Pair(complementarySum * (1 - 1.0 / (value1 / value2 + 1)), complementarySum / (value1 / value2 + 1))
}