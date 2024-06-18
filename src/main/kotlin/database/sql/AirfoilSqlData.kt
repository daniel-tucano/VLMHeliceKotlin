package database.sql

import airfoil.AirfoilGeometry
import airfoil.AirfoilPure
import airfoil.Polar
import plane.elements.Point2D
import plane.functions.LinearSpline
import javax.persistence.*

@Entity
@Table(name = "Airfoils")
data class AirfoilSqlData (
    @Id
    @Column(name = "AirfoilID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val airfoilID: Long? = null,
    @Column(name = "Name")
    val name: String? = null,
    @Column(name = "Thickness")
    val thickness: Double? = null,
    @Column(name = "X_Thickness")
    val xThickness: Double? = null,
    @Column(name = "Camber")
    val camber: Double? = null,
    @Column(name = "X_Camber")
    val xCamber: Double? = null,
    @ElementCollection
    @CollectionTable(
        name="Geometries",
        joinColumns= [JoinColumn(name = "AirfoilID")]
    )
    val geometrie: MutableList<AirfoilGeometrieSqlData>? = null,
    @OneToMany(mappedBy = "airfoilId")
    val runs: MutableList<RunSqlData>? = null
) {
    fun toPureAirfoil(): AirfoilPure {
        val topPoints = mutableListOf<Point2D>()
        val bottomPoints = mutableListOf<Point2D>()
        geometrie?.forEach {
            if (it.side == "Top") {
                topPoints.add(Point2D(it.x!!, it.y!!))
            } else {
                bottomPoints.add(Point2D(it.x!!, it.y!!))
            }
        }
        val airfoilGeometrie = AirfoilGeometry(topPoints, bottomPoints)

        val polars: MutableList<Polar> = mutableListOf()

        runs?.forEach { run ->

            val clAlphaPoints: MutableList<Point2D> = mutableListOf()
            val cdAlphaPoints: MutableList<Point2D> = mutableListOf()
            val cmAlphaPoints: MutableList<Point2D> = mutableListOf()

            run.polar?.forEach { polarPoint ->
                clAlphaPoints.add(Point2D(polarPoint.alfa!!, polarPoint.cl!!))
                cdAlphaPoints.add(Point2D(polarPoint.alfa, polarPoint.cd!!))
                if (polarPoint.cm != null) {
                    cmAlphaPoints.add(Point2D(polarPoint.alfa, polarPoint.cm))
                }
            }

            polars.add(Polar(
                reynolds = run.reynolds!!,
                mach = run.mach!!,
                cl = LinearSpline(clAlphaPoints),
                cd = LinearSpline(cdAlphaPoints),
                cm = LinearSpline(cmAlphaPoints)
            ))
        }

        return AirfoilPure(airfoilGeometrie, this.name!!, polars)
    }
}