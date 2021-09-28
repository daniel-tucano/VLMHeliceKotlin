package units

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AngularVelocityTest {
    @Test
    fun `must give the correct tangential velocity when using RPM`() {
        val angularVelocity = AngularVelocity.RPM(60.0)
        val tangentialVelocity1 = angularVelocity.tangentialVelocity(1.0)
        val tangentialVelocity2 = angularVelocity.tangentialVelocity(1.5)

        assertEquals(2*Math.PI, tangentialVelocity1)
        assertEquals(1.5*2*Math.PI, tangentialVelocity2)
    }
}