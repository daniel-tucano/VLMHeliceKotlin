package materials

data class Fluid (
    val density: Double,
    val dynamicViscosity: Double,
    val speedOfSound: Double
) {
    val kinematicViscosity: Double = dynamicViscosity/density
}