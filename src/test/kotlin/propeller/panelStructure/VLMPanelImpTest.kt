package propeller.panelStructure

import aerodynamics.vlm.HorseShoeVortex
import aerodynamics.vlm.VortexLine
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import space.elements.Direction3D
import space.elements.Point3D

internal class VLMPanelImpTest {
//    @Test
//    fun `must calculate correctly the influence on Point`() {
//        val panelPoints = listOf(
//            Point3D(0.0, 1.0, 0.0),
//            Point3D(1.0, 1.0, 0.0),
//            Point3D(1.0, 0.0, 0.0),
//            Point3D(0.0, 0.0, 0.0),
//        )
//        val panel = VLMPanelImp(
//            panelPoints, Direction3D.MAIN_Z_DIRECTION.toVector(Point3D(0.5, 0.25, 0.0)),
//            HorseShoeVortex(
//                fixedVortexLines = listOf(
//                    VortexLine.SurfaceVortex(
//                        headPosition = Point3D(0.0, 1.0, 0.0),
//                        tailPosition = Point3D(0.0, 0.0, 0.0)
//                    ),
//                    VortexLine.BoundVortex(
//                        headPosition = Point3D(1.0, 1.0, 0.0),
//                        tailPosition = Point3D(0.0, 1.0, 0.0)
//                    ),
//                    VortexLine.SurfaceVortex(
//                        headPosition = Point3D(1.0, 0.0, 0.0),
//                        tailPosition = Point3D(1.0, 1.0, 0.0)
//                    )
//                )
//            )
//        )
//
//        val influenceOnControlPoint = panel.influenceLineMatrixOnPointAndDirection(panel.controlPoint, panel.surfaceNormal.direction)
//
//        assertEquals(-0.203601171586652 * 2,influenceOnControlPoint[0], 10.0e-5)
//    }
}