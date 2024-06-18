package database.sql

import airfoil.AirfoilPure
import materials.Materials
import plane.elements.Point2D
import plane.functions.LinearSpline
import propeller.geometry.Propeller
import propeller.geometry.PropellerBladeNormalizedAirfoilDistribution
import propeller.geometry.PropellerBladeNormalizedGeometricDistributions
import propeller.geometry.PropellerBladeNormalizedGeometry
import javax.persistence.*

@Entity
@Table(name = "Props")
data class PropellerSqlData(
    @Id
    @Column(name = "propID")
    val propId: Long? = null,
    @Column(name = "name")
    val name: String? = null,
    @Column(name = "diameter")
    val diameter: Double? = null,
    @Column(name = "pitch")
    val pitch: Double? = null,
    @ElementCollection
    @CollectionTable(
        name="GeometricDistributions",
        joinColumns= [JoinColumn(name = "PropID")]
    )
    val geometricDistribution: MutableList<PropellerGeometricDistributionSqlData> = mutableListOf()
) {
    fun toPropeller(airfoil: AirfoilPure): Propeller {
        val airfoilDistribution = PropellerBladeNormalizedAirfoilDistribution(
            startAirfoil = airfoil,
            endAirfoil = airfoil
        )

        val incidenceAngleDistributionPoints = mutableListOf<Point2D>()
        val chordRadiusNormalizedDistributionPoints = mutableListOf<Point2D>()
        val sweepRadiusNormalizedPoints = mutableListOf<Point2D>()

        this.geometricDistribution.forEach { geometricDistributionPoint ->
            val newNormalizedRadius = (geometricDistributionPoint.normalizedRadius!! - 0.15) / (1.0 - 0.15)
            incidenceAngleDistributionPoints.add(Point2D(newNormalizedRadius, geometricDistributionPoint.beta!!))
            chordRadiusNormalizedDistributionPoints.add(Point2D(newNormalizedRadius, geometricDistributionPoint.normalizedChord!!))
            sweepRadiusNormalizedPoints.add(Point2D(newNormalizedRadius, geometricDistributionPoint.normalizedSweep!!))
        }

        val normalizedGeometry = PropellerBladeNormalizedGeometry(
            hubRadiusPercentage = 0.15,
            PropellerBladeNormalizedGeometricDistributions(
                airfoilDistribution = airfoilDistribution,
                incidenceAngleDistribution = LinearSpline(incidenceAngleDistributionPoints),
                chordRadiusNormalizedDistribution = LinearSpline(chordRadiusNormalizedDistributionPoints),
                sweepRadiusNormalizedDistribution = LinearSpline(sweepRadiusNormalizedPoints)
            )
        )

        return Propeller(
            numberOfBlades = 2,
            radius = (this.diameter!!/2) * 0.0254,
            bladeNormalizedGeometry = normalizedGeometry,
            material = Materials.MDF_WOOD
        )
    }
}