package propeller

import units.AngularVelocity

data class PropellerOperationPoint(
    val axialVelocity: Double,
    val angularVelocity: AngularVelocity
)