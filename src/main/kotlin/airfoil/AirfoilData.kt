package airfoil

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import plane.elements.Point2D

data class AirfoilData(
    val name: String,
    val airfoilID: Int,
    val geometrie: AirfoilDataGeometrie,
    val camber: Double,
    val thickness: Double,
    val xThickness: Double,
    val xCamber: Double,
    val runs: AirfoilDataRuns
) {
    enum class AirfoilDataGeometrieSide { Top, Bottom }
    data class AirfoilDataGeometrie(
        val side: List<AirfoilDataGeometrieSide>,
        var x: List<Double>,
        var y: List<Double>
    )

    data class AirfoilDataRuns(
        val runIDs: List<Long>
    )

    fun toPureAirfoil(runCollection: MongoCollection<RunData>): AirfoilPure {
        return AirfoilPure(AirfoilGeometry(
            this.geometrie.x
                .mapIndexedNotNull { index, x -> Point2D(x, this.geometrie.y[index]).takeIf { geometrie.side[index] == AirfoilDataGeometrieSide.Top } },
            this.geometrie.x
                .mapIndexedNotNull { index, x -> Point2D(x, this.geometrie.y[index]).takeIf { geometrie.side[index] == AirfoilDataGeometrieSide.Bottom } }
        ),
            name,
            runCollection.find(and(RunData::airfoilID eq airfoilID, RunData::source eq "Xfoil")).toList().map { it.toPolar() }
        )
    }
}