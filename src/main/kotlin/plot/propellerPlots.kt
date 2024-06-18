package plot

import com.mathworks.engine.MatlabEngine
import extensions.toList
import extensions.xValues
import extensions.yValues
import extensions.zValues
import org.ejml.simple.SimpleMatrix
import propeller.geometry.Propeller
import propeller.geometry.PropellerBlade
import propeller.panelStructure.PropellerPanel
import propeller.panelStructure.PropellerPanelBlade
import propeller.solvedPanelSctructure.SolvedPropellerPanel
import space.ParametricSurface3D
import utils.linspace
import utils.meshgrid

val matlabEngine: MatlabEngine = MatlabEngine.connectMatlab()


fun plotSolvedPropellerPanelForces(
    solvedPropellerPanel: SolvedPropellerPanel,
    scale: Double = 1.0
) {
    matlabEngine.eval("hold on")
    val positions = solvedPropellerPanel.panelsForces.map { it.position }
    val xComponents = solvedPropellerPanel.panelsForces.map { it.x * scale}.toDoubleArray()
    val yComponents = solvedPropellerPanel.panelsForces.map { it.y * scale}.toDoubleArray()
    val zComponents = solvedPropellerPanel.panelsForces.map { it.z * scale}.toDoubleArray()
    val xPositions = positions.xValues.toDoubleArray()
    val yPositions = positions.yValues.toDoubleArray()
    val zPositions = positions.zValues.toDoubleArray()
    matlabEngine.feval<Any>("quiver3", xPositions, yPositions, zPositions, xComponents, yComponents, zComponents)
}

fun plotPropellerPanel(
    propellerPanel: PropellerPanel
) {
    matlabEngine.eval("figure")
    matlabEngine.eval("hold on")
    propellerPanel.propellerPanelBlades.forEach {
        plotPropellerPanelBlade(it)
    }
    matlabEngine.eval("set(gca,'CameraViewAngleMode','Manual');")
    matlabEngine.eval("axis equal")
    matlabEngine.eval("grid minor")
}

fun plotPropellerPanelBlade(
    propellerPanelBlade: PropellerPanelBlade
) {
    plotPropellerBladeCamberSurface(
        propellerPanelBlade.propellerBladeBase,
        propellerPanelBlade.t,
        propellerPanelBlade.s
    )
}

fun plotPropellerSurface(
    propeller: Propeller,
    t: List<Double> = linspace(0.0, 1.0, 30),
    s: List<Double> = linspace(0.0, 1.0, 15)
) {
    matlabEngine.eval("figure")
    matlabEngine.eval("hold on")
    propeller.blades.forEach {
        plotPropellerBladeSurface(it, t, s)
    }
    matlabEngine.eval("set(gca,'CameraViewAngleMode','Manual');")
    matlabEngine.eval("axis equal")
    matlabEngine.eval("grid minor")
}

fun plotPropellerBladeSurface(
    blade: PropellerBlade,
    t: List<Double> = linspace(0.0, 1.0, 30),
    s: List<Double> = linspace(0.0, 1.0, 15)
) {
    val (T, S) = meshgrid(t, s)
    plotParametricSurface3D(blade.bladeParametricSurface, T, S)
}

fun plotPropellerCamberSurface(
    propeller: Propeller,
    t: List<Double> = linspace(0.0, 1.0, 30),
    s: List<Double> = linspace(0.0, 1.0, 15)
) {
    matlabEngine.eval("figure")
    matlabEngine.eval("hold on")
    propeller.blades.forEach {
        plotPropellerBladeCamberSurface(it, t, s)
    }
    matlabEngine.eval("set(gca,'CameraViewAngleMode','Manual');")
    matlabEngine.eval("axis equal")
    matlabEngine.eval("grid minor")
}

fun plotPropellerBladeCamberSurface(
    blade: PropellerBlade,
    t: List<Double> = linspace(0.0, 1.0, 30),
    s: List<Double> = linspace(0.0, 1.0, 15)
) {
    val (T, S) = meshgrid(t, s)
    plotParametricSurface3D(blade.bladeCamberParametricSurface, T, S)
}

fun plotParametricSurface3D(
    parametricSurface3D: ParametricSurface3D,
    T: SimpleMatrix,
    S: SimpleMatrix,
) {
    val (X, Y, Z) = parametricSurface3D(T, S)
    matlabEngine.feval<Any>("mesh", X.toDoubleMatrix(), Y.toDoubleMatrix(), Z.toDoubleMatrix())
}

fun SimpleMatrix.toDoubleMatrix(): Array<DoubleArray> {
    val doubleMatrix = Array(this.numRows()) { DoubleArray(this.numCols()) { 0.0 } }
    this.forEachRowIndexed { iRow, simpleMatrix ->
        doubleMatrix[iRow] = simpleMatrix.toList().toDoubleArray()
    }
    return doubleMatrix
}

fun SimpleMatrix.forEachRowIndexed(operation: (Int, SimpleMatrix) -> Unit) {
    for (iRow in 0 until this.numRows()) {
        operation(iRow, this.rows(iRow, iRow+1))
    }
}