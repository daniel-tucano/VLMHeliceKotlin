package database.sql

import javax.persistence.*

@Entity
@Table(name = "Runs")
data class RunSqlData (
    @Id
    @Column(name = "RunID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val runId: Long? = null,
    @Column(name = "AirfoilID")
    val airfoilId: Long? = null,
    @Column(name = "Reynolds")
    val reynolds: Double? = null,
    @Column(name = "Mach")
    val mach: Double? = null,
    @ElementCollection
    @CollectionTable(
        name="Polars",
        joinColumns= [JoinColumn(name = "RunID")]
    )
    val polar: MutableList<PolarSqlData>? = null
)