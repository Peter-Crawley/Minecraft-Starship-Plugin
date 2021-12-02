package io.github.petercrawley.minecraftstarshipplugin

import io.github.petercrawley.minecraftstarshipplugin.commands.CommandTabComplete
import io.github.petercrawley.minecraftstarshipplugin.commands.Commands
import io.github.petercrawley.minecraftstarshipplugin.customMaterials.CustomBlocksListener
import io.github.petercrawley.minecraftstarshipplugin.customMaterials.MSPMaterial
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.plugin.java.JavaPlugin

class MinecraftStarshipPlugin : JavaPlugin() {
	companion object {
		lateinit var plugin: MinecraftStarshipPlugin
			private set

		var customBlocks = mapOf<Byte, String>()
			private set

//		var timeOperations: Boolean = false
//			private set
//
//		var detectionLimit: Int = 500000
//			private set
//
//		var forcedUndetectable = setOf<MSPMaterial>()
//			private set
//
//		var defaultUndetectable = setOf<MSPMaterial>()
//			private set
	}

	override fun onEnable() {
		plugin = this

		Metrics(this, 12863)

		saveDefaultConfig()
		reloadConfig()

		getPluginManager().registerEvents(CustomBlocksListener(), this)

		plugin.getCommand("msp")!!.setExecutor(Commands())
		plugin.getCommand("msp")!!.tabCompleter = CommandTabComplete()
	}

	override fun reloadConfig() {
		super.reloadConfig()

		// Load custom blocks
		val newCustomBlocks = mutableMapOf<Byte, String>()
		config.getConfigurationSection("customBlocks")?.getKeys(false)?.forEach {
			var id = 0

			id += if (config.getBoolean("customBlocks.$it.north")) 32 else 0
			id += if (config.getBoolean("customBlocks.$it.east"))  16 else 0
			id += if (config.getBoolean("customBlocks.$it.south"))  8 else 0
			id += if (config.getBoolean("customBlocks.$it.west"))   4 else 0
			id += if (config.getBoolean("customBlocks.$it.up"))     2 else 0
			id += if (config.getBoolean("customBlocks.$it.down"))   1 else 0

			newCustomBlocks[id.toByte()] = it.uppercase()
		}

		customBlocks = newCustomBlocks

		config.getConfigurationSection("multiblocks")?.getKeys(false)?.forEach { multiblock ->
			// The first thing that needs to be done is we need to get all the keys for the multiblock
			// This way we know what blocks are in the multiblock
			val keys = mutableMapOf<String, MSPMaterial>()
			var interfaceKey: Char? = null

			for (key in config.getConfigurationSection("multiblocks.$multiblock.key")!!.getKeys(false)) {
				val materialString = config.getString("multiblocks.$multiblock.key.$key")!!

				val material = MSPMaterial(materialString)

				if (keys.containsValue(material)) {
					logger.severe("Multiblock $multiblock contains duplicate material $materialString")
					return@forEach
				}

				// TODO: Interface should be determined by a config file.
				if (materialString == "INTERFACE") interfaceKey = key[0]

				keys[key] = material
			}

			if (interfaceKey == null) {
				logger.severe("Multiblock $multiblock does not have an interface block")
				return@forEach
			}

			// Now we need to find the interface as all blocks in a multtiblock are stored relative to this point.
			val layers = config.getConfigurationSection("multiblocks.$multiblock.layers")!!.getKeys(false)

			var y: Int? = null
			var z: Int? = null
			var x: Int? = null

			layerLoop@for ((iY, layer) in layers.withIndex()) {
				val layerZList = config.getStringList("multiblocks.$multiblock.layers.$layer")

				var iZ = -1
				for (zString in layerZList) {
					iZ++

					var iX = -1

					for (c in zString) {
						iX++

						if (c == interfaceKey) {
							y = iY
							z = iZ
							x = iX

							break@layerLoop
						}
					}
				}
			}

			logger.info("Multiblock $multiblock has interface at $x, $y, $z")
		}

//		timeOperations = config.getBoolean("timeOperations", false)
//		detectionLimit = config.getInt("detectionLimit", 500000)
//
//		val newForcedUndetectable = mutableSetOf<MSPMaterial>()
//		config.getStringList("forcedUndetectable").forEach {
//			newForcedUndetectable.add(MSPMaterial(it))
//		}
//		forcedUndetectable = newForcedUndetectable
//
//		val newDefaultUndetectable = mutableSetOf<MSPMaterial>()
//		config.getStringList("defaultUndetectable").forEach {
//			newDefaultUndetectable.add(MSPMaterial(it))
//		}
//		defaultUndetectable = newDefaultUndetectable
	}
}
