package propeller.panelStructure

import aerodynamics.vlm.VLMPanel
import aerodynamics.vlm.HorseShoeVortex
import extensions.*
import materials.Fluid
import org.ejml.simple.SimpleMatrix
import space.VectorField3D
import space.elements.Direction3D
import space.elements.Point3D
import space.elements.Vector3D

class VLMPanelImp(
    val points: List<Point3D>,
    val surfaceNormal: Vector3D,
    var horseShoeVortex: HorseShoeVortex
) : VLMPanel {

    var gamma: Double = horseShoeVortex.gamma
        set(value) {
            horseShoeVortex.gamma = value
            field = value
        }

    //  Panel points must come in the following sequence
    //              ________
    //            0|		|1
    //             |		|
    //            3|________|2
    val controlPoint: Point3D = (points[0] + points[1] + points[2] * 3.0 + points[3] * 3.0) / 8.0

    val quarterCordMiddlePoint: Point3D = (points[0] * 3.0 + points[1] * 3.0 + points[2] + points[3]) / 8.0

    override fun XYZInfluenceMatrixOn(point: Point3D): SimpleMatrix {
        return horseShoeVortex.XYZInfluenceMatrixOn(point)
    }

    override fun influenceLineMatrixOnPointAndDirection(point: Point3D, direction: Direction3D): SimpleMatrix {
        return SimpleMatrix(arrayOf(doubleArrayOf(horseShoeVortex.influenceOnPointAndDirection(point, direction))))
    }

    override fun calculateRightHandSide(freeStream: VectorField3D): SimpleMatrix {
        return SimpleMatrix(1, 1, false, doubleArrayOf(-surfaceNormal.dot(freeStream(controlPoint))))
    }

    fun calculateForces(velocity: Vector3D, fluid: Fluid): List<Vector3D> {
        val (fx, fy, fz) = horseShoeVortex.calculateForce(velocity, fluid)
        return listOf( Vector3D(fx,fy,fz, quarterCordMiddlePoint) )
    }

}