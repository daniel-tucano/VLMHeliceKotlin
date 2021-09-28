package aerodynamics.vlm

import extensions.toList
import materials.Fluid
import org.ejml.simple.SimpleMatrix
import space.elements.Direction3D
import space.elements.Point3D
import space.elements.Vector3D

sealed class VortexLine(
    var gamma: Double = 1.0,
    headPosition: Point3D,
    val tailPosition: Point3D
) : Vector3D(headPosition, tailPosition) {

    open fun XYZInfluenceMatrixOn(point: Point3D): SimpleMatrix {
        val a = Vector3D(headPosition = point, tailPosition = this.position)
        val b = Vector3D(headPosition = point, tailPosition = this.headPosition)
        val divisor = (a.module * b.module + (a dot b))
        if (divisor <= 10e-6) {
            return SimpleMatrix(3, 1)
        }
        return (((a cross b) / divisor * ((1 / a.module + 1 / b.module))) / (4 * Math.PI)).matrix
    }

    fun influenceLineMatrixOnPointAndDirection(point: Point3D, direction: Direction3D): Double {
        return XYZInfluenceMatrixOn(point).dot(direction.matrix)
    }

    fun reverseVortexLineDirection(): VortexLine {
        return when (this) {
            is BoundVortex -> BoundVortex(gamma, headPosition = tailPosition, tailPosition = headPosition)
            is SurfaceVortex -> SurfaceVortex(gamma, headPosition = tailPosition, tailPosition = headPosition)
            is FreeStreamVortex -> FreeStreamVortex(gamma, headPosition = tailPosition, tailPosition = headPosition)
        }
    }

    companion object {
        fun List<VortexLine>.reverseVortexLinesDirections(): List<VortexLine> {
            return this.reversed().map { it.reverseVortexLineDirection() }
        }
    }

    class BoundVortex(
        gamma: Double = 1.0,
        headPosition: Point3D,
        tailPosition: Point3D
    ) : VortexLine(gamma, headPosition, tailPosition) {
        fun calculateForce(velocity: Vector3D, fluid: Fluid): Vector3D {
            return (velocity cross this) * fluid.density * gamma
        }
    }

    class SurfaceVortex(
        gamma: Double = 1.0,
        headPosition: Point3D,
        tailPosition: Point3D
    ) : VortexLine(gamma, headPosition, tailPosition)

    class FreeStreamVortex(
        gamma: Double = 1.0,
        headPosition: Point3D,
        tailPosition: Point3D
    ) : VortexLine(gamma, headPosition, tailPosition)
}
