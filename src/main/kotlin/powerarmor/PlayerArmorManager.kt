package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.PowerArmorModule
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class PlayerArmorManager(val player: Player) {

	val wearingPowerArmor: Boolean
		get() {
			return (PowerArmorManager.isPowerArmor(player.inventory.helmet) &&
					PowerArmorManager.isPowerArmor(player.inventory.chestplate) &&
					PowerArmorManager.isPowerArmor(player.inventory.leggings) &&
					PowerArmorManager.isPowerArmor(player.inventory.boots))

		}

	var modules = mutableSetOf<PowerArmorModule>()
		get() {
			// Load the player's modules from their PersistentDataContainer
			val moduleCSV = player.persistentDataContainer.get(
				NamespacedKey(MinecraftStarshipPlugin.plugin, "equipped-power-armor-modules"),
				PersistentDataType.STRING
			) ?: return mutableSetOf<PowerArmorModule>()
			val moduleNames = moduleCSV.split(",")
			val modules = mutableSetOf<PowerArmorModule>()
			moduleNames.forEach {
				val module = PowerArmorManager.getModuleFromName(it)
				if (module != null) {
					modules.add(module)
				}
			}
			return modules
		}
		set(value) {
			// Save the player's modules to their PersistentDataContainer
			var moduleCSV = "" // Comma separated values of all the module names

			value.forEach {
				moduleCSV += it.name + ","
			}
			player.persistentDataContainer.set(
				NamespacedKey(MinecraftStarshipPlugin.plugin, "equipped-power-armor-modules"),
				PersistentDataType.STRING,
				moduleCSV
			)
			field = value
		}

	var armorPower: Int
		get() {
			return player.persistentDataContainer.get(
				NamespacedKey(MinecraftStarshipPlugin.plugin, "power-armor-power"),
				PersistentDataType.INTEGER
			)
				?: return 0
		}
		set(value) {
			var newPower = value // can't modify val
			if (newPower > PowerArmorManager.maxPower) newPower = PowerArmorManager.maxPower
			player.persistentDataContainer.set(
				NamespacedKey(MinecraftStarshipPlugin.plugin, "power-armor-power"),
				PersistentDataType.INTEGER,
				newPower
			)
		}

	val moduleWeight: Int
		get() {
			var weight = 0
			modules.forEach {
				weight += it.weight
			}
			return weight
		}


	var armorEnabled: Boolean
		get() {
			return (player.persistentDataContainer.get(
				NamespacedKey(MinecraftStarshipPlugin.plugin, "power-armor-enabled"),
				PersistentDataType.INTEGER
			) == 1)
		}
		set(value) {
			player.persistentDataContainer.set(
				NamespacedKey(MinecraftStarshipPlugin.plugin, "power-armor-enabled"),
				PersistentDataType.INTEGER,
				if (value) 1 else 0
			)
		}

	fun addModule(module: PowerArmorModule) {
		modules = (modules + module).toMutableSet()
	}
	fun removeModule(module: PowerArmorModule) {
		module.disableModule(player)
		modules = (modules - module).toMutableSet()
	}
}
