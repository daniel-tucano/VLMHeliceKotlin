package propeller.geometry

import airfoil.AirfoilMixed
import airfoil.AirfoilPure
import utils.isNormalizedValue

data class PropellerBladeNormalizedAirfoilDistribution(
    val startAirfoil: AirfoilPure,
    val endAirfoil: AirfoilPure,
    val middleAirfoils: List<AirfoilNormallyPositioned>? = null
) {
    init {
        if (middleAirfoils != null && middleAirfoils.any { it.position == 0.0 || it.position == 1.0 }) throw IllegalArgumentException(
            "Middle Airfoils normalized positions cant be 1.0 or 0.0 because this are start and end airfoils normalized positions"
        )
    }

    private val airfoilsNormallyPositioned: List<AirfoilNormallyPositioned>
        get() {
            if (middleAirfoils != null) {
                return listOf(
                    AirfoilNormallyPositioned(
                        startAirfoil,
                        0.0
                    )
                ) + middleAirfoils.sortedBy { it.position } + listOf(AirfoilNormallyPositioned(endAirfoil, 1.0))
            }
            return listOf(AirfoilNormallyPositioned(startAirfoil, 0.0)) + listOf(
                AirfoilNormallyPositioned(
                    endAirfoil,
                    1.0
                )
            )
        }

    operator fun invoke(t: Double): AirfoilMixed {
        val (innerAirfoilNormallyPositioned, outerAirfoilNormallyPositioned) = airfoilsNormallyPositioned.innerAndOuterAirfoilsNormallyPositioned(
            t
        )
        val percentageOfOuterAirfoil = calculatePercentageOfOuterAirfoil(
            innerAirfoilNormallyPositioned.position,
            outerAirfoilNormallyPositioned.position,
            t
        )
        return AirfoilMixed(
            innerAirfoilNormallyPositioned.airfoil,
            outerAirfoilNormallyPositioned.airfoil,
            percentageOfOuterAirfoil
        )
    }

    private fun List<AirfoilNormallyPositioned>.innerAndOuterAirfoilsNormallyPositioned(t: Double): Pair<AirfoilNormallyPositioned, AirfoilNormallyPositioned> {
        if (!isNormalizedValue(t)) throw IllegalArgumentException("t must be a normalized value between 0.0 and 1.0")
        if (t <= 0.0) {
            return Pair(airfoilsNormallyPositioned.first(), airfoilsNormallyPositioned[1])
        } else if (t >= 1.0) {
            return Pair(airfoilsNormallyPositioned[this.lastIndex - 1], airfoilsNormallyPositioned.last())
        }
        val outerAirfoilIndex = airfoilsNormallyPositioned.indexOfFirst { t <= it.position }
        return Pair(airfoilsNormallyPositioned[outerAirfoilIndex - 1], airfoilsNormallyPositioned[outerAirfoilIndex])
    }

    private fun calculatePercentageOfOuterAirfoil(
        innerAirfoilNormalPosition: Double,
        outerAirfoilNormalPosition: Double,
        t: Double
    ): Double {
        return (t - innerAirfoilNormalPosition) / (outerAirfoilNormalPosition - innerAirfoilNormalPosition)
    }

    data class AirfoilNormallyPositioned(val airfoil: AirfoilPure, val position: Double)
}