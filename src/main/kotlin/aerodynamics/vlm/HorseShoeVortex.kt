package aerodynamics.vlm

import extensions.xValues
import extensions.yValues
import extensions.zValues
import io.github.danielTucano.matplotlib.Axes3D
import io.github.danielTucano.matplotlib.Figure
import io.github.danielTucano.matplotlib.pyplot.figure
import materials.Fluid
import org.ejml.simple.SimpleMatrix
import space.elements.Direction3D
import space.elements.Point3D
import space.elements.Vector3D
import java.awt.Color

class HorseShoeVortex(
    gamma: Double = 1.0,
    val fixedVortexLines: List<VortexLine>,
    upStreamVortexFilament: List<VortexLine>,
    downStreamVortexFilament: List<VortexLine>
) {

    var vortexLines = upStreamVortexFilament + fixedVortexLines + downStreamVortexFilament

    var upStreamVortexFilament = upStreamVortexFilament
        set(value) {
            field = value
            vortexLines = value + fixedVortexLines + downStreamVortexFilament
        }

    var downStreamVortexFilament = downStreamVortexFilament
        set(value) {
            field = value
            vortexLines = upStreamVortexFilament + fixedVortexLines + value
        }

    var gamma = gamma
        set(value) {
            vortexLines.forEach { it.gamma = value }
            field = value
        }

    fun XYZInfluenceMatrixOn(point: Point3D): SimpleMatrix {
        return vortexLines.map { it.XYZInfluenceMatrixOn(point) }.reduce { acc, simpleMatrix ->
            acc + simpleMatrix
        }
    }

    fun influenceOnPointAndDirection(point: Point3D, direction: Direction3D): Double {
        return vortexLines.map { it.influenceLineMatrixOnPointAndDirection(point, direction) }
            .reduce { acc, vortexInfluence ->
                acc + vortexInfluence
            }
    }

    fun calculateForce(velocity: Vector3D, fluid: Fluid): Vector3D {
        return vortexLines.filterIsInstance<VortexLine.BoundVortex>()
            .map { it.calculateForce(velocity, fluid) }.reduce { acc, vector3D -> acc + vector3D }
    }

    fun addPlotCommands(
        figure: Figure = figure(),
        axes: Axes3D = figure.add_subplot(projection = Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
    ): Pair<Figure, Axes3D> {

        val positions = this.vortexLines.map { it.position }
        val xComponents = this.vortexLines.map { it.x }
        val yComponents = this.vortexLines.map { it.y }
        val zComponents = this.vortexLines.map { it.z }

        axes.quiver(
            positions.xValues,
            positions.yValues,
            positions.zValues,
            xComponents,
            yComponents,
            zComponents,
            colors = listOf(Color.RED)
        )

        return  figure to axes
    }
}