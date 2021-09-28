package propeller.solvedPanelSctructure

import io.github.danielTucano.matplotlib.pyplot.grid
import io.github.danielTucano.matplotlib.pyplot.plot
import io.github.danielTucano.matplotlib.pyplot.xlim
import io.github.danielTucano.matplotlib.show
import io.github.danielTucano.python.pythonExecution
import materials.Fluid
import propeller.panelStructure.PropellerPanelBlade
import propeller.panelStructure.VLMPanelImp
import space.VectorField3D
import space.elements.Direction3D
import space.elements.Vector3D

class SolvedPropellerPanelBlade(
    val propellerPanelBlade: PropellerPanelBlade,
    val freeStream: VectorField3D,
    val fluid: Fluid
) {

    val solvedPropellerPanelStations = propellerPanelBlade.panelStations.map { SolvedPropellerPanelStation(it,freeStream, fluid) }

    internal val panelsForces: List<Vector3D> = solvedPropellerPanelStations.flatMap {
        it.panelsForces
    }
    internal val panelsThrustComponent = solvedPropellerPanelStations.map {
        it.totalThrust
    }
    internal val panelsAxialTorques: List<Double> = solvedPropellerPanelStations.map {
        it.totalTorque
    }

    val totalThrust = panelsThrustComponent.sum()
    val totalTorque = panelsAxialTorques.sum()

    fun plotThrustDistribution() {
        val nStations = solvedPropellerPanelStations.lastIndex
        val x = (0..nStations).toList()
        val y = solvedPropellerPanelStations.map { it.totalThrust }
        pythonExecution {
            plot(x,y)
            xlim(0.0, nStations.toDouble())
            grid()
            show()
        }
    }

}