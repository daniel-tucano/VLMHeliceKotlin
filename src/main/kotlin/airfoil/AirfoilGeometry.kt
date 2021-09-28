package airfoil

import plane.Curve2D
import plane.Polygon2D
import plane.elements.Point2D
import plane.functions.LinearSpline
import plane.utils.toPoint2DList
import utils.lerpList
import utils.linspace
import kotlin.math.max
import kotlin.math.min

class AirfoilGeometry(val topPoints: List<Point2D>, val bottomPoints: List<Point2D>) :
    Polygon2D(topPoints + bottomPoints) {
    val topSpline: LinearSpline = LinearSpline(topPoints.sortedBy { it.x })
    val bottomSpline: LinearSpline = LinearSpline(bottomPoints.sortedBy { it.x })
    val camber: Curve2D
        get() {
            val xValues = linspace(
                max(this.topPoints.minOf { it.x }, this.bottomPoints.minOf { it.x })*1.005,
                min(this.topPoints.maxOf { it.x }, this.bottomPoints.maxOf { it.x })*0.995,
                50
            )
            return Curve2D(
                xValues.mapIndexed { index, x ->
                    Point2D(x, (topSpline(x) + bottomSpline(x)) / 2.0)
                }
            )
        }
    val thickness: Curve2D
        get() {
            val xValues = linspace(
                max(this.topPoints.minOf { it.x }, this.bottomPoints.minOf { it.x })*1.005,
                min(this.topPoints.maxOf { it.x }, this.bottomPoints.maxOf { it.x })*0.995,
                50
            )
            return Curve2D(
                linspace(0.0, 1.0, max(topPoints.size, bottomPoints.size)).mapIndexed { index, x ->
                    Point2D(x, topSpline(x) - bottomSpline(x))
                }
            )
        }

    fun scale(scalar: Double): AirfoilGeometry {
        return AirfoilGeometry(
            topPoints.map { Point2D(it.x * scalar, it.y - this.centroid.y * scalar) },
            bottomPoints.map {
                Point2D(
                    it.x * scalar,
                    it.y * scalar
                )
            },
        )
    }

    override fun translateTo(newCentroid: Point2D): AirfoilGeometry {
        return AirfoilGeometry(
            topPoints.map { Point2D(it.x - this.centroid.x + newCentroid.x, it.y - this.centroid.y + newCentroid.y) },
            bottomPoints.map {
                Point2D(
                    it.x - this.centroid.x + newCentroid.x,
                    it.y - this.centroid.y + newCentroid.y
                )
            },
        )
    }

    fun mixAirfoilGeometries(
        otherAirfoilGeometrie: AirfoilGeometry,
        otherAirfoilFraction: Double,
        nPoints: Int = 50
    ): AirfoilGeometry {
        val xValues = linspace(
            max(
                max(this.topPoints.minOf { it.x }, this.bottomPoints.minOf { it.x }),
                max(otherAirfoilGeometrie.topPoints.minOf { it.x }, otherAirfoilGeometrie.bottomPoints.minOf { it.x })
            )*1.005,
            min(
                min(this.topPoints.maxOf { it.x }, this.bottomPoints.maxOf { it.x }),
                min(otherAirfoilGeometrie.topPoints.maxOf { it.x }, otherAirfoilGeometrie.bottomPoints.maxOf { it.x })
            )*0.995,
            nPoints
        )
        val xValuesReversed = xValues.reversed()
        return AirfoilGeometry(
            Pair(
                xValuesReversed,
                lerpList(
                    this.topSpline(xValuesReversed),
                    otherAirfoilGeometrie.topSpline(xValuesReversed),
                    otherAirfoilFraction
                )
            ).toPoint2DList(),
            Pair(
                xValues,
                lerpList(
                    this.bottomSpline(xValues),
                    otherAirfoilGeometrie.bottomSpline(xValues),
                    otherAirfoilFraction
                )
            ).toPoint2DList()
        )
    }
}