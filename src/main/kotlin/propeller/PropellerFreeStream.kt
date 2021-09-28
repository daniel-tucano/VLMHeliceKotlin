package propeller

import space.VectorField3D
import space.elements.Point3D
import space.elements.Vector3D

class PropellerFreeStream(
    private val propellerOperationPoint: PropellerOperationPoint
) : VectorField3D {
    override fun invoke(x: Double, y: Double, z: Double): Vector3D {
        val point = Point3D(x, y, z)
        val cylindricalCoordinate = point.toCylindricalCoordinate()
        val (axialVelocity, angularVelocity) = propellerOperationPoint
        val tangentialVelocity = angularVelocity.tangentialVelocity(cylindricalCoordinate.radius)
        return Vector3D(
            -cylindricalCoordinate.tangentDirection.x * tangentialVelocity,
            -cylindricalCoordinate.tangentDirection.y * tangentialVelocity,
            -axialVelocity,
            point
        )
    }
}