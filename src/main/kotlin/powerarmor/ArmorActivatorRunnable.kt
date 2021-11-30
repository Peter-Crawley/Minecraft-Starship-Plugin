package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import org.bukkit.Bukkit.getServer
import org.bukkit.scheduler.BukkitRunnable

class ArmorActivatorRunnable : BukkitRunnable() {

	override fun run() {
		// Iterate through all the players and activate their armor modules
		// if they're wearing a full set of power armor and have power left.
		// Disable them otherwise
		getServer().onlinePlayers.forEach { player ->
			val playerManager = PlayerPowerArmor(player)
			if (playerManager.wearingPowerArmor && playerManager.armorEnabled && playerManager.armorPower > 0 && playerManager.moduleWeight <= PowerArmorManager.maxModuleWeight) {
				playerManager.modules.forEach { module ->
					module.enableModule(player)
				}
			} else {
				playerManager.modules.forEach { module ->
					module.disableModule(player)
				}
			}
		}
	}
}