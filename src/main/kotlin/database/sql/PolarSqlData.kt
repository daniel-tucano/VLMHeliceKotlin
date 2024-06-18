package database.sql

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class PolarSqlData (
    @Column(name = "AirfoilID")
    val airfoilId: Long? = null,
    @Column(name = "Alpha")
    val alfa: Double? = null,
    @Column(name = "Cl")
    val cl: Double? = null,
    @Column(name = "Cd")
    val cd: Double? = null,
    @Column(name = "Cm")
    val cm: Double? = null
)