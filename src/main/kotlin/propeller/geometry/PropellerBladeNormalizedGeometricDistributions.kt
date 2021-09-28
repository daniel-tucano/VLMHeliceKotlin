package propeller.geometry

import airfoil.AirfoilMixed
import plane.functions.Function2D
import units.Angle

data class PropellerBladeNormalizedGeometricDistributions(
    val airfoilDistribution: PropellerBladeNormalizedAirfoilDistribution,
    val incidenceAngleDistribution: Function2D,
    val chordRadiusNormalizedDistribution: Function2D,
    val sweepRadiusNormalizedDistribution: Function2D
) {
    data class PropellerBladeDimensionedGeometricValues (
        val airfoil: AirfoilMixed,
        val incidenceAngle: Angle.Degrees,
        val chord: Double,
        val sweep: Double
    )

    fun dimensionedValues(radiusFraction: Double, radius: Double = 1.0): PropellerBladeDimensionedGeometricValues {
        return PropellerBladeDimensionedGeometricValues(
            airfoilDistribution(radiusFraction),
            Angle.Degrees(incidenceAngleDistribution(radiusFraction)),
            chordRadiusNormalizedDistribution(radiusFraction) * radius,
            sweepRadiusNormalizedDistribution(radiusFraction) * radius
        )
    }
}