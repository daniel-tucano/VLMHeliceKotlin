package database.sql

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class PropellerGeometricDistributionSqlData(
    @Column(name = "normalizedRadius")
    val normalizedRadius: Double? = null,
    @Column(name = "normalizedChord")
    val normalizedChord: Double? = null,
    @Column(name = "normalizedSweep")
    val normalizedSweep: Double? = null,
    @Column(name = "beta")
    val beta: Double? = null
)
