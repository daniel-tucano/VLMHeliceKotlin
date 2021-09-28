package aerodynamics.vlm

import propeller.PropellerOperationPoint
import space.VectorField3D
import space.elements.Point3D
import space.primitives.Helix
import units.Angle

/**
 * Represents a vortex sheet positioned at the free stream
 * @param wakeGrid -> must be provided with a list which its elements are spanwise positioned lists of points towards
 * downstream
 */
class FreeStreamWakeVortexSheet(
    val wakeGrid: MutableList<MutableList<Point3D>>
) {

    constructor(
        trailingEdgePoints: List<Point3D>,
        operationPoint: PropellerOperationPoint,
        nVortexFilamentsStreamwisePoints: Int,
        nFreeStreamVortexTurns: Double = 1.0,
    ) : this(
        buildFreeStreamInitialWakePoints(
            trailingEdgePoints,
            operationPoint,
            nVortexFilamentsStreamwisePoints,
            nFreeStreamVortexTurns
        )
    )

    companion object {
        fun buildFreeStreamInitialWakePoints(
            trailingEdgePoints: List<Point3D>,
            operationPoint: PropellerOperationPoint,
            nVortexFilamentsStreamwisePoints: Int,
            nFreeStreamVortexTurns: Double = 1.0,
        ): MutableList<MutableList<Point3D>> {
            val freeStreamAxialDistanceTraveled =
                operationPoint.axialVelocity * operationPoint.angularVelocity.timeToTravel(Angle.Radians(2 * Math.PI * nFreeStreamVortexTurns))
            return trailingEdgePoints.map { trailingEdgePoint ->
                val trailingEdgeCylidricalCoordinate = trailingEdgePoint.toCylindricalCoordinate()
                Helix(
                    nVortexFilamentsStreamwisePoints.toUInt(),
                    trailingEdgeCylidricalCoordinate.radius,
                    -freeStreamAxialDistanceTraveled,
                    nFreeStreamVortexTurns,
                    trailingEdgeCylidricalCoordinate.angle,
                    trailingEdgeCylidricalCoordinate.z,
                    false
                ).points.toMutableList()
            }.toMutableList()
        }
    }

    operator fun get(iSpanwise: Int, iStreamwise: Int): Point3D {
        return wakeGrid[iSpanwise][iStreamwise]
    }

    /**
     * returns a list of free stream vortex lines at the spanwise provided index pointing towards the downstram direction
     */
    operator fun get(iSpanwise: Int): List<VortexLine.FreeStreamVortex> {
        return wakeGrid[iSpanwise].drop(1).mapIndexed { iStreamwise, point3D ->
            VortexLine.FreeStreamVortex(
                headPosition = point3D, tailPosition = this[iSpanwise, iStreamwise]
            )
        }
    }

    fun addSpanwiseWakePointsAtTheEnd(inducedFreeStream: VectorField3D, dt: Double) {
        wakeGrid.forEachIndexed { iStreamwise, vortexFilament ->
            val lastPoint = vortexFilament.last()
            val velocity = inducedFreeStream(lastPoint)
            vortexFilament.add(
                Point3D(lastPoint.x + velocity.x * dt, lastPoint.y + velocity.y * dt, lastPoint.z + velocity.z * dt)
            )
        }
    }

    /**
     * Drop last points on the streamwise direction, move all other points by induced free stream velocity by dt units
     * of time, keeps the first streamwise point
     */
    fun addWakePointsTimeIteration(inducedFreeStream: VectorField3D, dt: Double): FreeStreamWakeVortexSheet {
        wakeGrid.forEach { vortexFilament ->
            vortexFilament.dropLast(1)
            val firstPoint = vortexFilament.first()
            vortexFilament.mapIndexed { iStreamwise, point ->
                val velocity = inducedFreeStream(point)
                vortexFilament[iStreamwise] = Point3D(point.x + velocity.x * dt, point.y + velocity.y * dt, point.z + velocity.z * dt)
            }
            vortexFilament.add(0, firstPoint)
        }
        return this
    }
}