import airfoil.AirfoilData
import airfoil.RunData
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import io.github.danielTucano.matplotlib.*
import io.github.danielTucano.matplotlib.pyplot.*
import io.github.danielTucano.python.pythonExecution
import materials.Fluids
import materials.Materials
import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import plane.addPlotCommands
import plane.elements.Point2D
import plane.functions.CubicSpline
import plane.functions.LinearSpline
import plane.utils.toPoint2DList
import propeller.PropellerFreeStream
import propeller.PropellerOperationPoint
import propeller.geometry.Propeller
import propeller.geometry.PropellerBladeNormalizedAirfoilDistribution
import propeller.geometry.PropellerBladeNormalizedGeometricDistributions
import propeller.geometry.PropellerBladeNormalizedGeometry
import propeller.panelStructure.PropellerPanel
import space.CoordinateSystem3D
import space.addPlotCommands
import units.AngularVelocity
import utils.linspace

fun main() {

    val client = KMongo.createClient(
        MongoClientSettings.builder().applyConnectionString(ConnectionString("mongodb://localhost:31190"))
            .credential(MongoCredential.createCredential("aerodb", "admin", "88430296".toCharArray())).build()
    )
    val database = client.getDatabase("aerodb")
    val airfoilCollection = database.getCollection<AirfoilData>(collectionName = "airfoils")
    val runCollection = database.getCollection<RunData>(collectionName = "runs")

    val clarkY = airfoilCollection.findOne(AirfoilData::airfoilID eq 130)!!.toPureAirfoil(runCollection)

//    val a18 = airfoilCollection.findOne(AirfoilData::airfoilID eq 3)!!.toPureAirfoil(runCollection)

//    val naca4412 = airfoilCollection.findOne(AirfoilData::airfoilID eq 1193)!!.toPureAirfoil(runCollection)

    val airfoilDistribution = PropellerBladeNormalizedAirfoilDistribution(
        clarkY, clarkY
    )

    // APC Sport 11x9 geometry distribution

    val angleDistribution = CubicSpline(
        listOf(
            36.20,
            42.16,
            45.18,
            43.27,
            39.74,
            36.31,
            33.07,
            30.20,
            27.70,
            25.55,
            23.63,
            21.85,
            20.18,
            18.93,
            17.71,
            16.88,
            15.31,
            13.67
        ).run {
            this.mapIndexed { index, d ->
                Point2D(index.toDouble() / this.lastIndex, d)
            }
        })

    val chordDistribution = CubicSpline(
        listOf(
            0.167,
            0.165,
            0.167,
            0.164,
            0.161,
            0.161,
            0.161,
            0.162,
            0.163,
            0.162,
            0.159,
            0.153,
            0.144,
            0.130,
            0.111,
            0.085,
            0.052,
            0.019,
        ).run {
            this.mapIndexed { index, d ->
                Point2D(index.toDouble() / this.lastIndex, d)
            }
        })

    val bladeGeometricDistributions = PropellerBladeNormalizedGeometricDistributions(
        airfoilDistribution,
        angleDistribution,
        chordDistribution,
        LinearSpline(Pair(linspace(0.0, 1.0, 3), listOf(0.0, 0.0, 0.0)).toPoint2DList())
    )

    val bladeNormalizedGeometry = PropellerBladeNormalizedGeometry(
        0.15,
        bladeGeometricDistributions
    )

    val diameter = 11 * 0.0254

    val propeller = Propeller(
        2,
        diameter / 2,
        bladeNormalizedGeometry,
        Materials.MDF_WOOD
    )

    val referenceAdvanceRatios = listOf(
        0.0924675324675324,
        0.112207792207792,
        0.135064935064935,
        0.156883116883116,
        0.179740259740259,
        0.204675324675324,
        0.227532467532467,
        0.25038961038961,
        0.272207792207792,
        0.297142857142857,
        0.322077922077922,
        0.343896103896103,
        0.361558441558441,
        0.382337662337662,
        0.407272727272727,
        0.433246753246753,
        0.450909090909091,
        0.475844155844155,
        0.498701298701298,
        0.52051948051948,
        0.542337662337662,
        0.569350649350649,
        0.587012987012987,
        0.611948051948051,
        0.63064935064935,
        0.656623376623376,
        0.676363636363636,
        0.702337662337662,
        0.722077922077922,
        0.747012987012987,
        0.765714285714285,
        0.793766233766233,
        0.81038961038961,
        0.837402597402597,
        0.855064935064935,
        0.883116883116883,
        0.901818181818181,
    )

    val referenceCts = listOf(
        0.12461139896373,
        0.124352331606217,
        0.123575129533678,
        0.122797927461139,
        0.122020725388601,
        0.120984455958549,
        0.122020725388601,
        0.120725388601036,
        0.119689119170984,
        0.117875647668393,
        0.116580310880829,
        0.115025906735751,
        0.112694300518134,
        0.111139896373056,
        0.109067357512953,
        0.106476683937823,
        0.104922279792746,
        0.102072538860103,
        0.099740932642487,
        0.0971502590673575,
        0.094041450777202,
        0.0904145077720207,
        0.088341968911917,
        0.0836787564766839,
        0.0803108808290155,
        0.0753886010362694,
        0.0715025906735751,
        0.0655440414507772,
        0.060880829015544,
        0.0546632124352331,
        0.0505181347150259,
        0.044300518134715,
        0.0404145077720207,
        0.0347150259067357,
        0.0316062176165803,
        0.0256476683937823,
        0.0227979274611399,
    )

    val referenceCtCurve = referenceAdvanceRatios.mapIndexed {index, referenceAdvanceRatio ->
        Point2D(referenceAdvanceRatio, referenceCts[index])
    }

    val advanceRatios = linspace(0.1,0.9,10)
    val angularVelocity = AngularVelocity.RPM(5800.0)
    val operationPoints = advanceRatios.map { advanceRatio ->
        PropellerOperationPoint(angularVelocity.toHertz().value * diameter * advanceRatio, angularVelocity)
    }

    val propellerCtCurve =
        operationPoints.map { operationPoint ->
            val freeStream = PropellerFreeStream(operationPoint)

            val propellerPanel = PropellerPanel(
                propeller,
                t = linspace(0.0, 1.0, 2),
                s = linspace(0.0, 1.0, 25),
                operationPoint = operationPoint,
                nFreeStreamVortexPoints = 25
            )

            propellerPanel.iterateWakeVortexSheet(8, relaxationConstant = 0.8)

            val solvedPropellerPanel = propellerPanel.solve(freeStream, Fluids.AIR)

            println("at ${operationPoint.axialVelocity} m/s -> ${solvedPropellerPanel.totalThrust} N")

            Point2D(solvedPropellerPanel.advanceRatio, solvedPropellerPanel.cT)
        }

    pythonExecution {
        val (fig, ax) = referenceCtCurve.addPlotCommands(kwargs = mapOf(Line2D.Line2DArgs.color to KwargValue.Quoted("g")))
        propellerCtCurve.addPlotCommands(fig,ax)
        xlim(0.0,1.2)
        ylim(0.0,0.15)
        grid()
        show()
    }

//    val operationPoint = PropellerOperationPoint(axialVelocity = 5.0, angularVelocity = AngularVelocity.RPM(5800.0))
//    val freeStream = PropellerFreeStream(operationPoint)
//
//    val propellerPanel = PropellerPanel(
//        propeller,
//        t = linspace(0.0, 1.0, 5),
//        s = linspace(0.0, 1.0, 25),
//        operationPoint = operationPoint,
//        nFreeStreamVortexPoints = 20
//    )
//
//    propellerPanel.iterateWakeVortexSheet(5, relaxationConstant = 0.2)
//
//    val solvedPropellerPanel = propellerPanel.solve(freeStream, Fluids.AIR)
//
//    println(solvedPropellerPanel.totalThrust)
//    println("for J = ${solvedPropellerPanel.advanceRatio} cT = ${solvedPropellerPanel.cT}")
//
//    pythonExecution {
//        val fig = figure()
//        val ax = fig.add_subplot(Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
////        propellerPanel.addPropellerBladeCamberSurfacePlotCommands(fig,ax)
//        propellerPanel.addHorseShoeVortexPlotCommands(fig, ax)
////        propellerPanel.addControlPointsPlotCommands(fig,ax)
////        propellerPanel.addSurfaceNormalsPlotCommands(fig,ax)
//        solvedPropellerPanel.addForcesPlotCommands(fig, ax)
////        freeStream.addPlotCommands(
////            linspace(-propeller.radius, propeller.radius, 8),
////            linspace(-propeller.radius, propeller.radius, 8),
////            linspace(-propeller.radius, propeller.radius, 8),
////            length = 0.01,
////            figure = fig,
////            axes = ax
////        )
//        xlim(-propeller.radius + propeller.radius / 2, propeller.radius + propeller.radius / 2)
//        ylim(-propeller.radius, propeller.radius)
//        ax.set_zlim3d(-propeller.radius, propeller.radius)
//        xlabel("X")
//        ylabel("Y")
//        ax.set_zlabel("Z")
//        CoordinateSystem3D.MAIN_3D_COORDINATE_SYSTEM.addPlotCommands(fig, ax, scale = 0.02)
//        show()
//    }

}