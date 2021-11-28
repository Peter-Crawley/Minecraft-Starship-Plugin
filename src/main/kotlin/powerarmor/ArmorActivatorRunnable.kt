package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import org.bukkit.Bukkit.getServer
import org.bukkit.scheduler.BukkitRunnable

class ArmorActivatorRunnable : BukkitRunnable() {

	override fun run() {
		// Iterate through all the players and activate their armor modules
		// if they're wearing a full set of power armor and have power left.
		// Disable them otherwise
		getServer().onlinePlayers.forEach { player ->
			if (PowerArmorManager.isWearingPowerArmor(player) && PowerArmorManager.getPowerArmorEnabled(player) && PowerArmorManager.getArmorPower(
					player
				) > 0 && PowerArmorManager.getCurrentModuleWeight(player) <= PowerArmorManager.maxModuleWeight
			) {
				PowerArmorManager.getModules(player).forEach { module ->
					module.enableModule(player)
				}
			} else {
				PowerArmorManager.getModules(player).forEach { module ->
					module.disableModule(player)
				}
			}
		}
	}
}