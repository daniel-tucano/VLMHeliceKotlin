package aerodynamics.vlm

import extensions.times
import materials.Fluid
import org.ejml.simple.SimpleMatrix
import space.VectorField3D
import space.elements.Direction3D
import space.elements.Point3D
import space.elements.Vector3D

interface VLMPanel {
    fun XYZInfluenceMatrixOn (point: Point3D): SimpleMatrix
    fun influenceLineMatrixOnPointAndDirection (point: Point3D, direction: Direction3D): SimpleMatrix {
        return direction.matrix.transpose() * XYZInfluenceMatrixOn(point)
    }
    fun calculateRightHandSide (freeStream: VectorField3D): SimpleMatrix
}