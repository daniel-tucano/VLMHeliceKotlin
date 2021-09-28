package airfoil

class AirfoilMixed(val airfoil1: AirfoilBase, val airfoil2: AirfoilBase, val percentageOfAirfoil2: Double) :
    AirfoilBase(airfoil1.geometrie.mixAirfoilGeometries(airfoil2.geometrie, percentageOfAirfoil2)) {

    override fun findPolarPoint(reynolds: Double, mach: Double, alpha: Double): Polar.PolarPoint {
        return Polar.PolarPoint.linearInterpolatePolarPoints(
            airfoil1.findPolarPoint(reynolds, mach, alpha),
            airfoil2.findPolarPoint(reynolds, mach, alpha),
            percentageOfAirfoil2
        )
    }
}