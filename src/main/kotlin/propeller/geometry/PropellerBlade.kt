package propeller.geometry

import io.github.danielTucano.matplotlib.Axes3D
import io.github.danielTucano.matplotlib.Figure
import io.github.danielTucano.matplotlib.pyplot.*
import io.github.danielTucano.matplotlib.show
import io.github.danielTucano.python.pythonExecution
import materials.Material
import plane.elements.Point2D
import space.ParametricSurface3D
import space.addPlotCommands
import space.elements.Direction3D.Companion.MAIN_X_DIRECTION
import space.elements.Direction3D.Companion.MAIN_Z_DIRECTION
import space.elements.Point3D
import space.functions.Function3D
import units.Angle
import utils.linspace
import utils.meshgrid

class PropellerBlade(
    val radius: Double,
    val normalizedGeometry: PropellerBladeNormalizedGeometry,
    val material: Material,
    val rotationPlaneAngle: Angle
) {
    val bladeParametricSurface = object : ParametricSurface3D(
        object : Function3D {
            override fun invoke(x: Double, y: Double): Double {
                val hubRadiusPercentage = normalizedGeometry.hubRadiusPercentage
                return (hubRadiusPercentage + y * (1 - hubRadiusPercentage)) * radius
            }
        },
        object : Function3D {
            override fun invoke(x: Double, y: Double): Double {
                val (airfoil, _, chord, sweep) = normalizedGeometry.geometricDistributions.dimensionedValues(y, radius)
                val airfoilGeometrie = airfoil.geometrie.scale(chord).translateTo(Point2D(sweep, 0.0))
                val airfoilGeometrieXParametricFunction = airfoilGeometrie.xParametricFunction()
                return -airfoilGeometrieXParametricFunction(x)
            }
        },
        object : Function3D {
            override fun invoke(x: Double, y: Double): Double {
                val (airfoil, _, chord, sweep) = normalizedGeometry.geometricDistributions.dimensionedValues(y, radius)
                val airfoilGeometrie = airfoil.geometrie.scale(chord).translateTo(Point2D(sweep, 0.0))
                val airfoilGeometrieYParametricFunction = airfoilGeometrie.yParametricFunction()
                return airfoilGeometrieYParametricFunction(x)
            }
        },
    ) {
        override operator fun invoke(t: Double, s: Double): Point3D {
            val (_, incidenceAngle, _, _) = normalizedGeometry.geometricDistributions.dimensionedValues(s,radius)
            return super.invoke(t, s)
                .rotate(MAIN_X_DIRECTION, incidenceAngle)
                .rotate(MAIN_Z_DIRECTION, rotationPlaneAngle)
        }
    }

    val bladeCamberParametricSurface = object : ParametricSurface3D(
        object : Function3D {
            override fun invoke(x: Double, y: Double): Double {
                val hubRadiusPercentage = normalizedGeometry.hubRadiusPercentage
                return (hubRadiusPercentage + y * (1 - hubRadiusPercentage)) * radius
            }

        },
        object : Function3D {
            override fun invoke(x: Double, y: Double): Double {
                val (airfoil, _, chord, sweep) = normalizedGeometry.geometricDistributions.dimensionedValues(y, radius)
                val airfoilGeometrie = airfoil.geometrie
                val camberXParametricFunction =
                    airfoilGeometrie.camber.scale(chord).translateTo(Point2D(sweep, 0.0)).xParametricFunction()
                return -camberXParametricFunction(x)
            }
        },
        object : Function3D {
            override fun invoke(x: Double, y: Double): Double {
                val (airfoil, _, chord, sweep) = normalizedGeometry.geometricDistributions.dimensionedValues(y,radius)
                val airfoilGeometrie = airfoil.geometrie
                val camberYParametricFunction =
                    airfoilGeometrie.camber.scale(chord).translateTo(Point2D(sweep, 0.0)).yParametricFunction()
                return camberYParametricFunction(x)
            }
        },
    ) {
        override operator fun invoke(t: Double, s: Double): Point3D {
            val (_, incidenceAngle, _, _) = normalizedGeometry.geometricDistributions.dimensionedValues(s,radius)
            return super.invoke(t, s)
                .rotate(MAIN_X_DIRECTION, incidenceAngle)
                .rotate(MAIN_Z_DIRECTION, rotationPlaneAngle)
        }
    }

    fun plot(t: List<Double> = linspace(0.0, 1.0, 30), s: List<Double> = linspace(0.0, 1.0, 15)) {
        pythonExecution {
            val (_, ax) = this.addPlotCommands(t, s)
            xlim(-radius, radius)
            ylim(-radius, radius)
            ax.set_zlim3d(-radius, radius)
            xlabel("X")
            ylabel("Y")
            ax.set_zlabel("Z")
            show()
        }

    }

    fun addPlotCommands(
        t: List<Double> = linspace(0.0, 1.0, 30),
        s: List<Double> = linspace(0.0, 1.0, 15),
        figure: Figure = figure(),
        axes: Axes3D = figure.add_subplot(projection = Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
    ): Pair<Figure, Axes3D> {
        val (T, S) = meshgrid(t, s)
        val (fig, ax) = bladeParametricSurface.addPlotCommands(
            T,
            S,
            figure = figure,
            axes = axes,
            colormap = ColorMaps.winter
        )
        return fig to ax
    }

    fun addCamberSurfacePlotCommands(
        t: List<Double> = linspace(0.0, 1.0, 30),
        s: List<Double> = linspace(0.0, 1.0, 15),
        figure: Figure = figure(),
        axes: Axes3D = figure.add_subplot(projection = Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
    ): Pair<Figure, Axes3D> {
        val (T, S) = meshgrid(t, s)
        val (fig, ax) = bladeCamberParametricSurface.addPlotCommands(
            T,
            S,
            figure = figure,
            axes = axes,
            colormap = ColorMaps.winter
        )
        return fig to ax
    }
}