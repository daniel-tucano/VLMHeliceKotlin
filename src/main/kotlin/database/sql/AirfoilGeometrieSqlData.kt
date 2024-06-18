package database.sql

import javax.persistence.*

@Embeddable
data class AirfoilGeometrieSqlData(
    @Column(name =  "X")
    val x: Double? = null,
    @Column(name =  "Y")
    val y: Double? = null,
    @Column(name =  "Side")
    val side: String? = null
)
