package propeller.solvedPanelSctructure

import extensions.*
import io.github.danielTucano.matplotlib.Axes3D
import io.github.danielTucano.matplotlib.Figure
import io.github.danielTucano.matplotlib.pyplot.figure
import materials.Fluid
import org.ejml.simple.SimpleMatrix
import propeller.panelStructure.PropellerPanel
import space.VectorField3D
import space.elements.Point3D
import space.elements.Vector3D
import java.awt.Color
import kotlin.math.pow

class SolvedPropellerPanel(
    private val propellerPanel: PropellerPanel,
    private val gammas: SimpleMatrix,
    private val freeStream: VectorField3D,
    private val fluid: Fluid
) {

    private val panelsQuarterCordMiddlePoints: List<Point3D> = propellerPanel.propellerPanelBlades.flatMap {
        it.panelsQuarterChordMiddlePoints
    }

    private val operationPoint = propellerPanel.operationPoint
    private val diameter = propellerPanel.propellerBase.radius * 2

    val solvedPropellerPanelBlades: List<SolvedPropellerPanelBlade> = propellerPanel.propellerPanelBlades.map {
        SolvedPropellerPanelBlade(it, freeStream + inducedVelocityVectorField(), fluid)
    }

    internal val panelsForces: List<Vector3D> = solvedPropellerPanelBlades.flatMap {
        it.panelsForces
    }
    internal val panelsThrustComponents: List<Double> = solvedPropellerPanelBlades.map {
        it.totalThrust
    }
    internal val panelsAxialTorques: List<Double> = solvedPropellerPanelBlades.map {
        it.totalTorque
    }

    val totalThrust = panelsThrustComponents.sum()
    val totalTorque = panelsAxialTorques.sum()

    val cT = totalThrust/ (fluid.density * operationPoint.angularVelocity.toHertz().value.pow(2) * diameter.pow(4))
    val advanceRatio = operationPoint.axialVelocity / (operationPoint.angularVelocity.toHertz().value * diameter)

    fun inducedVelocityVectorField(): VectorField3D {
        return object : VectorField3D {
            override fun invoke(x: Double, y: Double, z: Double): Vector3D {
                val position = Point3D(x,y,z)
                val (vx,vy,vz) = propellerPanel.XYZInfluenceMatrixOn(position) * gammas
                return Vector3D(vx,vy,vz, position)
            }
        }
    }

    fun addForcesPlotCommands (
        figure: Figure = figure(),
        axes: Axes3D = figure.add_subplot(projection = Figure.AddSubplotProjectionOptions.`3d`) as Axes3D,
        scale: Double = propellerPanel.propellerBase.radius/3
    ): Pair<Figure, Axes3D> {

        val positions = panelsForces.map { it.position }
        val xComponents = panelsForces.map { it.x * scale}
        val yComponents = panelsForces.map { it.y * scale}
        val zComponents = panelsForces.map { it.z * scale}

        axes.quiver(
            positions.xValues,
            positions.yValues,
            positions.zValues,
            xComponents,
            yComponents,
            zComponents,
            colors = listOf(Color.BLUE)
        )

        return figure to axes
    }

}