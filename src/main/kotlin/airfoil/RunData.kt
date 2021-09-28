package airfoil

import plane.elements.Point2D
import plane.functions.LinearSpline

data class RunData(
    val airfoilID: Int,
    val runID: Long,
    val reynolds: Double,
    val mach: Double,
    val polar: RunPolarData,
    val source: String
) {

    data class RunPolarData(
        val alpha: List<Double>,
        val cl: List<Double>,
        val cd: List<Double>,
        val cm: List<Double>
    )

    fun toPolar(): Polar {
        val (alphas, cls, cds, cms) = polar
        return Polar(
            reynolds,
            mach,
            LinearSpline(
                alphas.mapIndexed {iAlpha, alpha ->
                    Point2D(alpha, cls[iAlpha])
                }
            ),
            LinearSpline(
                alphas.mapIndexed {iAlpha, alpha ->
                    Point2D(alpha, cds[iAlpha])
                }
            ),
            LinearSpline(
                alphas.mapIndexed {iAlpha, alpha ->
                    Point2D(alpha, cms[iAlpha])
                }
            ),
        )
    }
}
