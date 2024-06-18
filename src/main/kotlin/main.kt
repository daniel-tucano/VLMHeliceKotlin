import com.mathworks.engine.MatlabEngine
import database.sql.AirfoilSqlData
import database.sql.PropellerSqlData
import io.github.danielTucano.matplotlib.*
import io.github.danielTucano.matplotlib.pyplot.*
import io.github.danielTucano.python.pythonExecution
import materials.Fluids
import propeller.PropellerFreeStream
import propeller.PropellerOperationPoint
import propeller.panelStructure.PropellerPanel
import space.CoordinateSystem3D
import space.addPlotCommands
import units.AngularVelocity
import utils.linspace
import javax.persistence.Persistence
import plot.plotPropellerPanel
import plot.plotSolvedPropellerPanelForces

fun main() {

    val entityManagerFactoryAeroDb = Persistence.createEntityManagerFactory("aerodb")
    val entityManagerAeroDb = entityManagerFactoryAeroDb.createEntityManager()
    val clarkY = entityManagerAeroDb.find(AirfoilSqlData::class.java, 130L)!!.toPureAirfoil()

    val entityManagerFactoryPropDb = Persistence.createEntityManagerFactory("propdb")
    val entityManagerPropDb = entityManagerFactoryPropDb.createEntityManager()
    val propeller = entityManagerPropDb.find(PropellerSqlData::class.java, 12L)!!.toPropeller(clarkY)

//    plotPropellerCamberSurface(
//        propeller,
//        linspace(0.0, 1.0, 50),
//        linspace(0.0, 1.0, 20)
//    )

    val operationPoint = PropellerOperationPoint(axialVelocity = 5.0, angularVelocity = AngularVelocity.RPM(5800.0))
    val freeStream = PropellerFreeStream(operationPoint)

    val propellerPanel = PropellerPanel(
        propeller,
        t = linspace(0.0, 1.0, 7),
        s = linspace(0.0, 1.0, 27),
        operationPoint = operationPoint,
        nFreeStreamVortexPoints = 20
    )

    plotPropellerPanel(propellerPanel)
    val solvedPropellerPanel = propellerPanel.solve(freeStream, Fluids.AIR)
    plotSolvedPropellerPanelForces(solvedPropellerPanel)

    println(solvedPropellerPanel.totalThrust)
    println("for J = ${solvedPropellerPanel.advanceRatio} cT = ${solvedPropellerPanel.cT}")

//    pythonExecution {
//        val fig = figure()
//        val ax = fig.add_subplot(Figure.AddSubplotProjectionOptions.`3d`) as Axes3D
//        propellerPanel.addPropellerBladeCamberSurfacePlotCommands(fig,ax)
////        propellerPanel.addHorseShoeVortexPlotCommands(fig, ax)
//        propellerPanel.addControlPointsPlotCommands(fig,ax)
////        propellerPanel.addSurfaceNormalsPlotCommands(fig,ax)
//        solvedPropellerPanel.addForcesPlotCommands(fig, ax, scale = 0.00001)
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