package airfoil

abstract class AirfoilBase(val geometrie: AirfoilGeometry) {
    abstract fun findPolarPoint(reynolds: Double, mach: Double, alpha: Double): Polar.PolarPoint
}