package airfoil

import plane.functions.Function2D
import utils.lerp

data class Polar(
    val reynolds: Double,
    val mach: Double,
    val cl: Function2D,
    val cd: Function2D,
    val cm: Function2D
) {
    data class PolarPoint(
        val alpha: Double,
        val cl: Double,
        val cd: Double,
        val cm: Double
    ) {
        companion object {
            fun linearInterpolatePolarPoints(
                polarPointStart: PolarPoint,
                polarPointEnd: PolarPoint,
                fractionOfEndPolarPoint: Double
            ): PolarPoint {
                val alpha = lerp(polarPointStart.alpha, polarPointEnd.alpha, fractionOfEndPolarPoint)
                val cl = lerp(polarPointStart.cl, polarPointEnd.cl, fractionOfEndPolarPoint)
                val cd = lerp(polarPointStart.cd, polarPointEnd.cd, fractionOfEndPolarPoint)
                val cm = lerp(polarPointStart.cm, polarPointEnd.cm, fractionOfEndPolarPoint)

                return PolarPoint(alpha, cl, cd, cm)
            }
        }
    }

    operator fun invoke(alpha: Double): PolarPoint {
        return PolarPoint(alpha, cl(alpha), cd(alpha), cm(alpha))
    }
}
