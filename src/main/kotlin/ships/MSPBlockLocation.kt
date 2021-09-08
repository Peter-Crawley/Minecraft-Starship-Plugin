package io.github.petercrawley.minecraftstarshipplugin.ships

import org.bukkit.Location
import org.bukkit.World

class MSPBlockLocation {
	var x: Int = 0
	var y: Int = 0
	var z: Int = 0

	constructor(x: Int, y: Int, z: Int) {
		this.x = x
		this.y = y
		this.z = z
	}

	constructor(bukkit: Location) {
		x = bukkit.blockX
		y = bukkit.blockY
		z = bukkit.blockZ
	}

	fun bukkit(world: World): Location {
		return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
	}

	fun add(x: Int, y: Int, z: Int): MSPBlockLocation {
		return MSPBlockLocation(this.x + x, this.y + y, this.z + z)
	}

	override fun equals(other: Any?): Boolean {
		if (other !is MSPBlockLocation) return false
		if (other.x != x) return false
		if (other.y != y) return false
		if (other.z != z) return false
		return true
	}

	// Don't know what this does but IntelliJ wanted it.
	// Originally IntelliJ generated it like this but I simplified it.
	//	override fun hashCode(): Int {
	//		var result = x
	//		result = 31 * result + y
	//		result = 31 * result + z
	//		return result
	//	}
	override fun hashCode(): Int {
		return 31 * (31 * x + y) + z
	}
}
