import extensions.mapIndexed
import io.github.danielTucano.matplotlib.Axes3D
import io.github.danielTucano.matplotlib.AxesBase
import io.github.danielTucano.matplotlib.Figure
import io.github.danielTucano.matplotlib.pyplot.*
import io.github.danielTucano.matplotlib.show
import io.github.danielTucano.python.pythonExecution
import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import plane.ParametricCurve2D
import plane.elements.Point2D
import plane.elements.xValues
import plane.elements.yValues
import plane.functions.LinearSpline
import space.ParametricSurface3D
import space.elements.Point3D
import space.functions.Function3D
import utils.linspace
import utils.meshgrid

enum class AirfoilGeometrieSide { Top, Bottom }
data class AirfoilGeometrie(val side: List<AirfoilGeometrieSide>, val x: List<Double>, val y: List<Double>)
data class Airfoil(val geometrie: AirfoilGeometrie, val airfoilID: Int, val name: String)

data class AirfoilGeometriePoint(val x: Double, val y: Double, val side: AirfoilGeometrieSide)

fun main() {

    val client = KMongo.createClient()
    val database = client.getDatabase("aerodb")
    val col = database.getCollection<Airfoil>(collectionName = "airfoils")
    val clarkY = col.findOne(Airfoil::airfoilID eq 130)

    val xLinearSpline = LinearSpline(clarkY!!.geometrie.x.mapIndexed { index, x ->
        Point2D(index.toDouble() / clarkY.geometrie.x.lastIndex, x)
    })

    val yLinearSpline = LinearSpline(clarkY.geometrie.y.mapIndexed { index, y ->
        Point2D(index.toDouble() / clarkY.geometrie.y.lastIndex, y)
    })

    val funcaoXAsa = object : Function3D {
        override fun invoke(x: Double, y: Double): Point3D {
            return Point3D(x, y, xLinearSpline(x).y)
        }
    }

    val funcaoYAsa = object : Function3D {
        override fun invoke(x: Double, y: Double): Point3D {
            return Point3D(x, y, y)
        }
    }

    val funcaoZAsa = object : Function3D {
        override fun invoke(x: Double, y: Double): Point3D {
            return Point3D(x, y, yLinearSpline(x).y)
        }
    }

    val superficieAsa = ParametricSurface3D(funcaoXAsa, funcaoYAsa, funcaoZAsa)

    val t = linspace(0.0, 1.0, 50)
    val s = linspace(0.0,1.0,10)
    val (T, S) = meshgrid(t, s)
    val X = T.mapIndexed { index, element ->
        superficieAsa(T[index], S[index]).x
    }
    val Y = T.mapIndexed { index, element ->
        superficieAsa(T[index], S[index]).y
    }
    val Z = T.mapIndexed { index, element ->
        superficieAsa(T[index], S[index]).z
    }

    pythonExecution {
        val fig = figure()
        val ax = fig.add_subplot(Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
        xlim(0.0,1.0)
        ylim(0.0,1.0)
        ax.set_zlim3d(-0.5,0.5)
        ax.plot_surface(X,Y,Z)
        show()
    }

//    val aerofolio = ParametricCurve2D(xLinearSpline, yLinearSpline)
//    val aerofolioPoints = aerofolio(t)
//
//    pythonExecution {
//        val (fig, ax) = subplots()
//        ax.plot(aerofolioPoints.xValues(), aerofolioPoints.yValues())
//        ax.set_aspect(AxesBase.AspectOptions.equal, AxesBase.AjustableOptions.datalim)
//        grid()
//        show()
//    }

}