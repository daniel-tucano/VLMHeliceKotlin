package propeller.panelStructure

import aerodynamics.vlm.VLMCompositePanel
import aerodynamics.vlm.VortexLine
import aerodynamics.vlm.VortexLine.Companion.reverseVortexLinesDirections
import extensions.times
import extensions.xValues
import extensions.yValues
import extensions.zValues
import io.github.danielTucano.matplotlib.*
import io.github.danielTucano.matplotlib.pyplot.figure
import materials.Fluid
import materials.Fluids
import org.ejml.simple.SimpleMatrix
import plane.addScatterPlotCommands
import propeller.PropellerFreeStream
import propeller.PropellerOperationPoint
import propeller.geometry.Propeller
import propeller.solvedPanelSctructure.SolvedPropellerPanel
import space.VectorField3D
import space.addPlotCommands
import space.elements.Direction3D
import space.elements.Point3D
import units.Angle
import units.AngularVelocity
import utils.linspace
import java.awt.Color

class PropellerPanel(
    val propellerBase: Propeller,
    val t: List<Double> = linspace(0.0, 1.0, 6),
    val s: List<Double> = linspace(0.0, 1.0, 26),
    val nFreeStreamVortexTurns: Double = 1.0,
    val nFreeStreamVortexPoints: Int = 25,
    operationPoint: PropellerOperationPoint = PropellerOperationPoint(1.0, AngularVelocity.RPM(1.0))
) : VLMCompositePanel {
    /**
     * Every time the operation point changes it reflects on the panels HorseShoeVortex geometries
     */
    var operationPoint = operationPoint
        set(value) {
            propellerPanelBlades.forEach {
                it.operationPoint = value
            }
            field = value
        }

    val propellerPanelBlades: List<PropellerPanelBlade> = propellerBase.blades.map {
        PropellerPanelBlade(it, t, s, nFreeStreamVortexTurns, nFreeStreamVortexPoints, operationPoint)
    }

    private val controlPoints = propellerPanelBlades.flatMap { it.controlPoints }
    private val surfaceNormals = propellerPanelBlades.flatMap { it.surfaceNormals }

    val horseShoeVorticesLines: List<VortexLine>
        get() {
            return propellerPanelBlades.flatMap {
                it.horseShoeVorticesLines
            }
        }

    fun iterateWakeVortexSheet(
        iterations: Int,
        dt: Double = operationPoint.angularVelocity.timeToTravel(
            Angle.Radians(2 * Math.PI * nFreeStreamVortexTurns)
        ) / nFreeStreamVortexPoints,
        relaxationConstant: Double = 1.0,
        fluid: Fluid = Fluids.AIR,
        freeStream: VectorField3D = PropellerFreeStream(operationPoint)
    ) {
        for (iteration in 0 until iterations) {
            val solvedPropellerPanel = solve(freeStream, fluid)
            println("${solvedPropellerPanel.totalThrust} at iteration $iteration")
            propellerPanelBlades.forEach {
                it.freeStreamWakeVortexSheet = it.freeStreamWakeVortexSheet.addWakePointsTimeIteration(
                    freeStream + solvedPropellerPanel.inducedVelocityVectorField(),
                    dt * relaxationConstant
                )
            }
        }
    }

    fun solve(freeStream: VectorField3D, fluid: Fluid): SolvedPropellerPanel {
        val gammas = calculateGammas(freeStream)
        return SolvedPropellerPanel(this, gammas, freeStream, fluid)
    }

    private fun calculateGammas(freeStream: VectorField3D): SimpleMatrix {
        val gammas = calculateFullInfluenceMatrix().invert() * calculateRightHandSide(freeStream)
        propellerPanelBlades.forEachIndexed { iBlade, panelBlade ->
            panelBlade.panelStations.forEachIndexed { iStation, panelStations ->
                panelStations.panels.forEachIndexed { iPanel, panel ->
                    panel as VLMPanelImp
                    panel.gamma = gammas[iBlade * s.lastIndex * t.lastIndex + iStation * t.lastIndex + iPanel]
                }
            }
        }
        return gammas
    }

    private fun calculateFullInfluenceMatrix(): SimpleMatrix {
        return this.controlPoints.mapIndexed { iControlPoint, controlPoint ->
            this.influenceLineMatrixOnPointAndDirection(controlPoint, surfaceNormals[iControlPoint].direction)
        }.reduce { acc, simpleMatrix ->
            acc.concatRows(simpleMatrix)
        }
    }

    override fun XYZInfluenceMatrixOn(point: Point3D): SimpleMatrix {
        return propellerPanelBlades.map { it.XYZInfluenceMatrixOn(point) }.reduce { acc, simpleMatrix ->
            acc.concatColumns(simpleMatrix)
        }
    }

    override fun influenceLineMatrixOnPointAndDirection(point: Point3D, direction: Direction3D): SimpleMatrix {
        return propellerPanelBlades.map { it.influenceLineMatrixOnPointAndDirection(point, direction) }
            .reduce { acc, simpleMatrix ->
                acc.concatColumns(simpleMatrix)
            }
    }

    override fun calculateRightHandSide(freeStream: VectorField3D): SimpleMatrix {
        return propellerPanelBlades.map { it.calculateRightHandSide(freeStream) }.reduce { acc, simpleMatrix ->
            acc.concatRows(simpleMatrix)
        }
    }

    fun addPropellerBladeCamberSurfacePlotCommands(
        figure: Figure = figure(),
        axes: Axes3D = figure.add_subplot(projection = Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
    ): Pair<Figure, Axes3D> {
        return propellerBase.addCamberSurfacePlotCommands(t, s, figure, axes)
    }

    fun addControlPointsPlotCommands(
        figure: Figure = figure(),
        axes: Axes3D = figure.add_subplot(projection = Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
    ): Pair<Figure, Axes3D> {
        controlPoints.addScatterPlotCommands(
            figure,
            axes,
            kwargs = mapOf(Axes3D.Scatter3DKwargsKeys.edgecolors to KwargValue.Quoted("g"))
        )

        return figure to axes
    }

    fun addSurfaceNormalsPlotCommands(
        figure: Figure = figure(),
        axes: Axes3D = figure.add_subplot(projection = Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
    ): Pair<Figure, Axes3D> {
        surfaceNormals.addPlotCommands(
            figure,
            axes,
            scale = 0.01
        )

        return figure to axes
    }

    fun addHorseShoeVortexPlotCommands(
        figure: Figure = figure(),
        axes: Axes3D = figure.add_subplot(projection = Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
    ): Pair<Figure, Axes3D> {
        val positions = this.horseShoeVorticesLines.map { it.position }
        val xComponents = this.horseShoeVorticesLines.map { it.x }
        val yComponents = this.horseShoeVorticesLines.map { it.y }
        val zComponents = this.horseShoeVorticesLines.map { it.z }

        axes.quiver(
            positions.xValues,
            positions.yValues,
            positions.zValues,
            xComponents,
            yComponents,
            zComponents,
            colors = listOf(Color.RED),
            kwargs = mapOf(Line2D.Line2DArgs.linewidth to KwargValue.Unquoted("0.1"))
        )

        return figure to axes
    }


}