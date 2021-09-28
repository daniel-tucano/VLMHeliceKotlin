package units

sealed class AngularVelocity {

    abstract val value: Double

    abstract fun tangentialVelocity(radius: Double): Double
    abstract fun timeToTravel (angle: Angle): Double

    abstract operator fun plus(value: Double): AngularVelocity
    abstract operator fun minus(value: Double): AngularVelocity
    abstract operator fun times(value: Double): AngularVelocity
    abstract operator fun div(value: Double): AngularVelocity

    abstract operator fun plus(angularVelocity: AngularVelocity): AngularVelocity
    abstract operator fun minus(angularVelocity: AngularVelocity): AngularVelocity
    abstract operator fun times(angularVelocity: AngularVelocity): AngularVelocity
    abstract operator fun div(angularVelocity: AngularVelocity): AngularVelocity

    abstract fun toRadiansPerSecond(): RadiansPerSecond
    abstract fun toHertz(): Hertz
    abstract fun toRPM(): RPM

    class RadiansPerSecond(override val value: Double) : AngularVelocity() {
        override fun tangentialVelocity(radius: Double): Double {
            return value * radius
        }

        override fun timeToTravel(angle: Angle): Double {
            return when (angle) {
                is Angle.Radians -> angle.value / this.value
                is Angle.Degrees -> angle.toRadians().value / this.value
            }
        }

        override fun toRadiansPerSecond(): RadiansPerSecond = this

        override fun toHertz(): Hertz {
            return Hertz(value / (2 * Math.PI))
        }

        override fun toRPM(): RPM {
            return RPM(value * 60.0 / (2 * Math.PI))
        }

        override fun plus(value: Double): RadiansPerSecond {
            return RadiansPerSecond(this.value + value)
        }

        override fun minus(value: Double): RadiansPerSecond {
            return RadiansPerSecond(this.value - value)
        }

        override fun times(value: Double): RadiansPerSecond {
            return RadiansPerSecond(this.value * value)
        }

        override fun div(value: Double): RadiansPerSecond {
            return RadiansPerSecond(this.value / value)
        }

        override fun plus(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this + angularVelocity
                is Hertz -> this + angularVelocity.toRadiansPerSecond()
                is RPM -> this + angularVelocity.toRadiansPerSecond()
            }
        }

        override fun minus(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this - angularVelocity
                is Hertz -> this - angularVelocity.toRadiansPerSecond()
                is RPM -> this - angularVelocity.toRadiansPerSecond()
            }
        }

        override fun times(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this * angularVelocity
                is Hertz -> this * angularVelocity.toRadiansPerSecond()
                is RPM -> this * angularVelocity.toRadiansPerSecond()
            }
        }

        override fun div(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this / angularVelocity
                is Hertz -> this / angularVelocity.toRadiansPerSecond()
                is RPM -> this / angularVelocity.toRadiansPerSecond()
            }
        }
    }

    class Hertz(override val value: Double) : AngularVelocity() {
        override fun tangentialVelocity(radius: Double): Double {
            return value * (2 * Math.PI * radius)
        }

        override fun timeToTravel(angle: Angle): Double {
            return when (angle) {
                is Angle.Radians -> angle.value / this.toRadiansPerSecond().value
                is Angle.Degrees -> angle.toRadians().value / this.toRadiansPerSecond().value
            }
        }

        override fun toRadiansPerSecond(): RadiansPerSecond {
            return RadiansPerSecond(value * (2 * Math.PI))
        }

        override fun toHertz(): Hertz = this

        override fun toRPM(): RPM {
            return RPM(value * 60.0)
        }

        override fun plus(value: Double): Hertz {
            return Hertz(this.value + value)
        }

        override fun minus(value: Double): Hertz {
            return Hertz(this.value - value)
        }

        override fun times(value: Double): Hertz {
            return Hertz(this.value * value)
        }

        override fun div(value: Double): Hertz {
            return Hertz(this.value / value)
        }

        override fun plus(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this + angularVelocity.toHertz()
                is Hertz -> this + angularVelocity
                is RPM -> this + angularVelocity.toHertz()
            }
        }

        override fun minus(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this - angularVelocity.toHertz()
                is Hertz -> this - angularVelocity
                is RPM -> this - angularVelocity.toHertz()
            }
        }

        override fun times(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this * angularVelocity.toHertz()
                is Hertz -> this * angularVelocity
                is RPM -> this * angularVelocity.toHertz()
            }
        }

        override fun div(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this / angularVelocity.toHertz()
                is Hertz -> this / angularVelocity
                is RPM -> this / angularVelocity.toHertz()
            }
        }
    }

    class RPM(override val value: Double) : AngularVelocity() {
        override fun tangentialVelocity(radius: Double): Double {
            return value / 60.0 * (2 * Math.PI * radius)
        }

        override fun timeToTravel(angle: Angle): Double {
            return when (angle) {
                is Angle.Radians -> angle.value / this.toRadiansPerSecond().value
                is Angle.Degrees -> angle.toRadians().value / this.toRadiansPerSecond().value
            }
        }

        override fun toRadiansPerSecond(): RadiansPerSecond {
            return RadiansPerSecond(value / 60.0 * (2 * Math.PI))
        }

        override fun toHertz(): Hertz {
            return Hertz(value / 60.0)
        }

        override fun toRPM(): RPM = this

        override fun plus(value: Double): RPM {
            return RPM(this.value + value)
        }

        override fun minus(value: Double): RPM {
            return RPM(this.value - value)
        }

        override fun times(value: Double): RPM {
            return RPM(this.value * value)
        }

        override fun div(value: Double): RPM {
            return RPM(this.value / value)
        }

        override fun plus(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this + angularVelocity.toRPM()
                is Hertz -> this + angularVelocity.toRPM()
                is RPM -> this + angularVelocity
            }
        }

        override fun minus(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this - angularVelocity.toRPM()
                is Hertz -> this - angularVelocity.toRPM()
                is RPM -> this - angularVelocity
            }
        }

        override fun times(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this * angularVelocity.toRPM()
                is Hertz -> this * angularVelocity.toRPM()
                is RPM -> this * angularVelocity
            }
        }

        override fun div(angularVelocity: AngularVelocity): AngularVelocity {
            return when (angularVelocity) {
                is RadiansPerSecond -> this / angularVelocity.toRPM()
                is Hertz -> this / angularVelocity.toRPM()
                is RPM -> this / angularVelocity
            }
        }
    }
}