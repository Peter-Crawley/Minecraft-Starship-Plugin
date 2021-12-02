package io.github.petercrawley.minecraftstarshipplugin

import io.github.petercrawley.minecraftstarshipplugin.commands.CommandTabComplete
import io.github.petercrawley.minecraftstarshipplugin.commands.Commands
import io.github.petercrawley.minecraftstarshipplugin.customMaterials.CustomBlocksListener
import io.github.petercrawley.minecraftstarshipplugin.customMaterials.MSPMaterial
import io.github.petercrawley.minecraftstarshipplugin.multiblocks.MultiblockConfiguration
import io.github.petercrawley.minecraftstarshipplugin.multiblocks.MultiblockOriginRelativeLocation
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.plugin.java.JavaPlugin

class MinecraftStarshipPlugin : JavaPlugin() {
	companion object {
		lateinit var plugin: MinecraftStarshipPlugin
			private set

		var customBlocks = mapOf<Byte, String>()
			private set

		var multiblocks = setOf<MultiblockConfiguration>()
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

		val newMultiblocks = mutableSetOf<MultiblockConfiguration>()
		config.getConfigurationSection("multiblocks")?.getKeys(false)?.forEach multiblockLoop@{ multiblock ->
			// The first thing that needs to be done is we need to get all the keys for the multiblock
			// This way we know what blocks are in the multiblock
			val keys = mutableMapOf<String, MSPMaterial>()
			var interfaceKey: Char? = null

			config.getConfigurationSection("multiblocks.$multiblock.key")!!.getKeys(false).forEach {
				val materialString = config.getString("multiblocks.$multiblock.key.$it")!!

				val material = MSPMaterial(materialString)

				if (keys.containsValue(material)) {
					logger.severe("Multiblock $multiblock contains duplicate material $materialString")
					return@multiblockLoop
				}

				// TODO: Interface should be determined by a config file.
				if (materialString == "INTERFACE") interfaceKey = it[0]

				keys[it] = material
			}

			if (interfaceKey == null) {
				logger.severe("Multiblock $multiblock does not have an interface block")
				return@multiblockLoop
			}

			// Now we need to find the interface as all blocks in a multtiblock are stored relative to this point.
			val layers = config.getConfigurationSection("multiblocks.$multiblock.layers")!!.getKeys(false)

			var interfaceY: Int? = null
			var interfaceZ: Int? = null
			var interfaceX: Int? = null

			run layerLoop@ {
				layers.forEachIndexed { y, yName ->
					config.getStringList("multiblocks.$multiblock.layers.$yName").forEachIndexed { z, zString ->
						zString.forEachIndexed { x, xChar ->
							if (xChar == interfaceKey) {
								interfaceY = y
								interfaceZ = z
								interfaceX = x

								return@layerLoop
							}
						}
					}
				}
			}

			// Create a MultiblockConfiguration
			val multiblockConfiguration = MultiblockConfiguration(multiblock)

			// Now we need to get all the blocks relative to the origin (interface)
			layers.forEachIndexed { y, yName ->
				config.getStringList("multiblocks.$multiblock.layers.$yName").forEachIndexed { z, zString ->
					zString.forEachIndexed { x, xChar ->
						// Find relative position
						val relativeY = y - interfaceY!!
						val relativeZ = z - interfaceZ!!
						val relativeX = x - interfaceX!!

						// Get the material from keys
						val material = keys[xChar.toString()]

						// Construct a MultiblockOriginRelativeLocation
						val location = MultiblockOriginRelativeLocation(relativeX, relativeY, relativeZ)

						// Add the block to the multiblock configuration
						multiblockConfiguration.blocks[location] = material!!
					}
				}
			}

			newMultiblocks.add(multiblockConfiguration)
		}

		multiblocks = newMultiblocks

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
