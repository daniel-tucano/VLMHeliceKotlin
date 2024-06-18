package propeller.solvedPanelSctructure

import materials.Fluid
import propeller.panelStructure.PropellerPanelStation
import propeller.panelStructure.VLMPanelImp
import space.VectorField3D
import space.elements.Direction3D
import space.elements.Vector3D

class SolvedPropellerPanelStation(
    val propellerPanelStation: PropellerPanelStation,
    val freeStream: VectorField3D,
    val fluid: Fluid
) {

    internal val panelsForces = calculateForces(freeStream, fluid)
    internal val panelsThrustComponent = panelsForces.map {
        it dot Direction3D.MAIN_Z_DIRECTION
    }
    internal val panelsAxialTorques: List<Double> = panelsForces.mapIndexed { index, vector3D ->
        (propellerPanelStation.panelsQuarterChordMiddlePoints[index].asVector3D() cross vector3D) dot Direction3D.MAIN_Z_DIRECTION
    }

    val totalThrust = panelsThrustComponent.sum()
    val totalTorque = panelsAxialTorques.sum()

    fun calculateForces(freeStream: VectorField3D, fluid: Fluid): List<Vector3D> {
        return propellerPanelStation.panels.flatMap {  panel ->
            panel as VLMPanelImp
            panel.calculateForces(freeStream(panel.quarterCordMiddlePoint), fluid)
        }
    }

}