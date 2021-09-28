package propeller.geometry

data class PropellerBladeNormalizedGeometry(
    val hubRadiusPercentage: Double,
    val geometricDistributions: PropellerBladeNormalizedGeometricDistributions,
) {
}