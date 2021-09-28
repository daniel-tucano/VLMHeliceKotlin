package propeller.panelStructure

import aerodynamics.vlm.VLMCompositePanel
import aerodynamics.vlm.VLMPanel
import aerodynamics.vlm.VortexLine
import extensions.times
import extensions.xValues
import extensions.yValues
import extensions.zValues
import io.github.danielTucano.matplotlib.Axes3D
import io.github.danielTucano.matplotlib.Figure
import io.github.danielTucano.matplotlib.KwargValue
import io.github.danielTucano.matplotlib.Line2D
import io.github.danielTucano.matplotlib.pyplot.figure
import materials.Fluid
import org.ejml.simple.SimpleMatrix
import plane.addPlotCommands
import space.VectorField3D
import space.addPlotCommands
import space.elements.Direction3D
import space.elements.Point3D
import space.elements.Vector3D
import units.AngularVelocity
import java.awt.Color

class PropellerPanelStation(
    val panels: List<VLMPanel>
) : VLMCompositePanel {
    val controlPoints: List<Point3D> = panels.map {
        it as VLMPanelImp
        it.controlPoint
    }

    val surfaceNormals: List<Vector3D> = panels.map {
        it as VLMPanelImp
        it.surfaceNormal
    }

    val panelsQuarterChordMiddlePoints: List<Point3D> = panels.map {
        it as VLMPanelImp
        it.quarterCordMiddlePoint
    }

    val horseShoeVorticesLines: List<VortexLine>
        get() {
            return panels.flatMap {
                it as VLMPanelImp
                it.horseShoeVortex.vortexLines
            }
        }

    override fun XYZInfluenceMatrixOn(point: Point3D): SimpleMatrix {
        return panels.map { it.XYZInfluenceMatrixOn(point) }.reduce { acc, simpleMatrix ->
            acc.concatColumns(simpleMatrix)
        }
    }

    override fun influenceLineMatrixOnPointAndDirection(point: Point3D, direction: Direction3D): SimpleMatrix {
        return panels.map { it.influenceLineMatrixOnPointAndDirection(point, direction) }.reduce { acc, simpleMatrix ->
            acc.concatColumns(simpleMatrix)
        }
    }

    override fun calculateRightHandSide(freeStream: VectorField3D): SimpleMatrix {
        return panels.map { it.calculateRightHandSide(freeStream) }.reduce { acc, simpleMatrix ->
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