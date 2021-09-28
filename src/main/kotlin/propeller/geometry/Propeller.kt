package propeller.geometry

import io.github.danielTucano.matplotlib.Axes3D
import io.github.danielTucano.matplotlib.Figure
import io.github.danielTucano.matplotlib.pyplot.*
import io.github.danielTucano.matplotlib.show
import io.github.danielTucano.python.pythonExecution
import materials.Material
import space.CoordinateSystem3D
import space.addPlotCommands
import units.Angle
import utils.linspace

data class Propeller(
    val numberOfBlades: Int,
    val radius: Double,
    val bladeNormalizedGeometry: PropellerBladeNormalizedGeometry,
    val material: Material
) {
    val blades: List<PropellerBlade>
        get() = (0 until numberOfBlades).map { index ->
            PropellerBlade(radius, bladeNormalizedGeometry, material, Angle.Degrees(360.0 / numberOfBlades * index))
        }

    fun plot(t: List<Double> = linspace(0.0, 1.0, 30), s: List<Double> = linspace(0.0, 1.0, 15)) {
        pythonExecution {
            val figure = figure()
            val axes = figure.add_subplot(projection = Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
            blades.forEach {
                it.addPlotCommands(t, s, figure, axes)
            }
            xlim(-radius, radius)
            ylim(-radius, radius)
            axes.set_zlim3d(-radius, radius)
            xlabel("X")
            ylabel("Y")
            axes.set_zlabel("Z")
            CoordinateSystem3D.MAIN_3D_COORDINATE_SYSTEM.addPlotCommands(figure, axes, scale = 0.02)
            show()
        }

    }

    fun plotCamberSurface(t: List<Double> = linspace(0.0, 1.0, 30), s: List<Double> = linspace(0.0, 1.0, 15)) {
        pythonExecution {
            val (figure, axes) = addCamberSurfacePlotCommands(t,s)
            xlim(-radius, radius)
            ylim(-radius, radius)
            axes.set_zlim3d(-radius, radius)
            xlabel("X")
            ylabel("Y")
            axes.set_zlabel("Z")
            CoordinateSystem3D.MAIN_3D_COORDINATE_SYSTEM.addPlotCommands(figure, axes, scale = 0.02)
            show()
        }

    }

    fun addCamberSurfacePlotCommands(
        t: List<Double> = linspace(0.0, 1.0, 30),
        s: List<Double> = linspace(0.0, 1.0, 15),
        figure: Figure = figure(),
        axes: Axes3D = figure.add_subplot(projection = Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
    ): Pair<Figure, Axes3D> {
        blades.forEach {
            it.addCamberSurfacePlotCommands(t, s, figure, axes)
        }

        return figure to axes
    }
}