package airfoil

import utils.lerp

class AirfoilPure(geometrie: AirfoilGeometry, val name: String, val polars: List<Polar>) : AirfoilBase(geometrie) {
    override fun findPolarPoint(reynolds: Double, mach: Double, alpha: Double): Polar.PolarPoint {
        val previousMachPolars = polars.filter { it.mach == polars.last { polar -> mach >= polar.mach }.mach }
        val nextMachPolars = polars.filter { it.mach == polars.first { polar -> mach <= polar.mach }.mach }

        val previousMachPolarPoint = mixPolarPointsByReynolds(previousMachPolars, reynolds, alpha)
        val nextMachPolarPoint = mixPolarPointsByReynolds(nextMachPolars, reynolds, alpha)

        return mixPolarPointsByMach(
            previousMachPolarPoint to nextMachPolarPoint,
            previousMachPolars[0].mach,
            nextMachPolars[0].mach,
            mach
        )
    }

    private fun mixPolarPointsByReynolds(polars: List<Polar>, reynolds: Double, alpha: Double): Polar.PolarPoint {
        val previousReynoldsPolar = polars.last { reynolds >= it.reynolds }
        val nextReynoldsPolar = polars.first { reynolds <= it.reynolds }

        val fractionOfNextReynolds =
            (reynolds - previousReynoldsPolar.reynolds) / (nextReynoldsPolar.reynolds - previousReynoldsPolar.reynolds)

        return Polar.PolarPoint.linearInterpolatePolarPoints(
            previousReynoldsPolar(alpha),
            nextReynoldsPolar(alpha),
            fractionOfNextReynolds
        )
    }

    private fun mixPolarPointsByMach(
        polarPoints: Pair<Polar.PolarPoint, Polar.PolarPoint>,
        nextMach: Double,
        previousMach: Double,
        mach: Double
    ): Polar.PolarPoint {
        val fractionOfNextMach =
            (mach - previousMach) / (nextMach - previousMach)

        val (previousMachPolarPoint, nextMachPolarPoint) = polarPoints

        return Polar.PolarPoint.linearInterpolatePolarPoints(
            previousMachPolarPoint,
            nextMachPolarPoint,
            fractionOfNextMach
        )
    }
}