package propeller.panelStructure

import aerodynamics.vlm.FreeStreamWakeVortexSheet
import aerodynamics.vlm.HorseShoeVortex
import aerodynamics.vlm.VLMCompositePanel
import aerodynamics.vlm.VortexLine
import aerodynamics.vlm.VortexLine.Companion.reverseVortexLinesDirections
import extensions.xValues
import extensions.yValues
import extensions.zValues
import io.github.danielTucano.matplotlib.Axes3D
import io.github.danielTucano.matplotlib.Figure
import io.github.danielTucano.matplotlib.KwargValue
import io.github.danielTucano.matplotlib.Line2D
import io.github.danielTucano.matplotlib.pyplot.figure
import org.ejml.simple.SimpleMatrix
import plane.addPlotCommands
import propeller.PropellerOperationPoint
import propeller.geometry.PropellerBlade
import space.VectorField3D
import space.elements.Direction3D
import space.elements.Point3D
import space.elements.Vector3D
import units.AngularVelocity
import utils.linspace
import java.awt.Color

class PropellerPanelBlade(
    val propellerBladeBase: PropellerBlade,
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
            field = value
            freeStreamWakeVortexSheet = FreeStreamWakeVortexSheet(
                s.map { propellerBladeBase.bladeCamberParametricSurface(t.last(), it) },
                value,
                nFreeStreamVortexPoints,
                nFreeStreamVortexTurns,
            )
        }

    val nPanelsPerStation = t.size - 1
    val nStations = s.size - 1

    private val controlPointsParametricCamberSurfaceParameters: Pair<List<Double>, List<Double>>
        get() {
            return Pair(
                t.mapIndexedNotNull { index, d -> if (index == t.lastIndex) null else d + 0.75 * (t[index + 1] - d) },
                s.mapIndexedNotNull { index, d -> if (index == s.lastIndex) null else d + 0.50 * (s[index + 1] - d) }
            )
        }

    var freeStreamWakeVortexSheet = FreeStreamWakeVortexSheet(
        s.map { propellerBladeBase.bladeCamberParametricSurface(t.last(), it) },
        operationPoint,
        nFreeStreamVortexPoints,
        nFreeStreamVortexTurns,
    )
        set(value) {
            field = value
            panelStations.forEachIndexed { iStation, panelStation ->
                panelStation.panels.forEachIndexed { iPanel, panel ->
                    panel as VLMPanelImp
                    panel.horseShoeVortex.upStreamVortexFilament = value[iStation].reverseVortexLinesDirections()
                    panel.horseShoeVortex.downStreamVortexFilament = value[iStation + 1]
                }
            }
        }

    val panelStations: List<PropellerPanelStation> = createPanelStations()

    private fun createPanelStations(): List<PropellerPanelStation> {
        val (tControlPoints, sControlPoints) = controlPointsParametricCamberSurfaceParameters
        return (0 until nStations).map { iStation ->
            PropellerPanelStation((0 until nPanelsPerStation).map { iPanel ->
                VLMPanelImp(
                    createPanelGeometryPoints(iStation, iPanel),
                    propellerBladeBase.bladeCamberParametricSurface.numericalNormalDirection(
                        tControlPoints[iPanel],
                        sControlPoints[iStation]
                    ),
                    HorseShoeVortex(
                        fixedVortexLines = createPanelFixedVortexLines(iStation, iPanel),
                        upStreamVortexFilament = freeStreamWakeVortexSheet[iStation].reverseVortexLinesDirections(),
                        downStreamVortexFilament = freeStreamWakeVortexSheet[iStation + 1]
                    )
                )
            })
        }
    }

    private fun createPanelGeometryPoints(iStation: Int, iPanel: Int): List<Point3D> {
        return listOf(
            propellerBladeBase.bladeCamberParametricSurface(t[iPanel], s[iStation]),
            propellerBladeBase.bladeCamberParametricSurface(t[iPanel], s[iStation + 1]),
            propellerBladeBase.bladeCamberParametricSurface(t[iPanel + 1], s[iStation + 1]),
            propellerBladeBase.bladeCamberParametricSurface(t[iPanel + 1], s[iStation])
        )
    }

    private fun createPanelFixedVortexLines(iStation: Int, iPanel: Int): List<VortexLine> {
        val innerLegSurfaceVortexLines = createSurfaceVortexLines(iStation, iPanel).reverseVortexLinesDirections()
        val outerLegSurfaceVortexLines = createSurfaceVortexLines(iStation + 1, iPanel)

        val boundVortexLine = VortexLine.BoundVortex(
            tailPosition = innerLegSurfaceVortexLines.last().headPosition,
            headPosition = outerLegSurfaceVortexLines.first().tailPosition
        )

        return innerLegSurfaceVortexLines + boundVortexLine + outerLegSurfaceVortexLines
    }

    private fun createSurfaceVortexLines(iStation: Int, iPanel: Int): List<VortexLine> {
        val sCamber = s[iStation]

        val legSurfaceVortexLinesPoints = t.filterIndexed { index, _ -> index >= iPanel }
            .mapIndexed { index, tValue ->
                when (index) {
                    0 -> findPanelCamberBoundVortexPoint(iStation, iPanel)
                    else -> propellerBladeBase.bladeCamberParametricSurface(tValue, sCamber)
                }
            }
        return legSurfaceVortexLinesPoints.dropLast(1).mapIndexed { index, point3D ->
            VortexLine.SurfaceVortex(tailPosition = point3D, headPosition = legSurfaceVortexLinesPoints[index + 1])
        }
    }

    private fun findPanelCamberBoundVortexPoint(iStation: Int, iPanel: Int): Point3D {
        val sCamber = s[iStation]
        val tPanelCamberLeadingEdge = t[iPanel]
        val tPanelCamberTrailingEdge = t[iPanel + 1]
        val panelCamberLeadingEdge = propellerBladeBase.bladeCamberParametricSurface(tPanelCamberLeadingEdge, sCamber)
        val panelCamberTrailingEdge = propellerBladeBase.bladeCamberParametricSurface(tPanelCamberTrailingEdge, sCamber)
        return panelCamberLeadingEdge * 0.75 + panelCamberTrailingEdge * 0.25
    }

    val controlPoints = panelStations.flatMap {
        it.controlPoints
    }

    val surfaceNormals: List<Vector3D> = panelStations.flatMap {
        it.surfaceNormals
    }

    val panelsQuarterChordMiddlePoints = panelStations.flatMap {
        it.panelsQuarterChordMiddlePoints
    }

    val horseShoeVorticesLines: List<VortexLine>
        get() {
            return panelStations.flatMap {
                it.horseShoeVorticesLines
            }
        }

    override fun XYZInfluenceMatrixOn(point: Point3D): SimpleMatrix {
        return panelStations.map { it.XYZInfluenceMatrixOn(point) }.reduce { acc, simpleMatrix ->
            acc.concatColumns(simpleMatrix)
        }
    }

    override fun influenceLineMatrixOnPointAndDirection(point: Point3D, direction: Direction3D): SimpleMatrix {
        return panelStations.map { it.influenceLineMatrixOnPointAndDirection(point, direction) }
            .reduce { acc, simpleMatrix ->
                acc.concatColumns(simpleMatrix)
            }
    }

    override fun calculateRightHandSide(freeStream: VectorField3D): SimpleMatrix {
        return panelStations.map { it.calculateRightHandSide(freeStream) }.reduce { acc, simpleMatrix ->
            acc.concatRows(simpleMatrix)
        }
    }

    fun addControlPointsPlotCommands(
        figure: Figure = figure(),
        axes: Axes3D = figure.add_subplot(projection = Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
    ): Pair<Figure, Axes3D> {
        controlPoints.addPlotCommands(
            figure,
            axes,
            kwargs = mapOf(
                Line2D.Line2DArgs.color to KwargValue.Quoted("g"),
                Line2D.Line2DArgs.linestyle to KwargValue.Quoted("o")
            )
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
            colors = listOf(Color.RED)
        )

        return figure to axes
    }

}