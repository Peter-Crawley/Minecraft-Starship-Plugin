package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import org.bukkit.Bukkit.getServer
import org.bukkit.scheduler.BukkitRunnable

class ArmorActivatorRunnable : BukkitRunnable() {

	override fun run() {
		// Iterate through all the players and activate their armor modules
		// if they're wearing a full set of power armor and have power left.
		// Disable them otherwise
		getServer().onlinePlayers.forEach { player ->
			player.sendMessage("\n \n \n \n \n \n \n \n \n \n \n \n \n \n \n")
			player.sendMessage("Wearing: ${PowerArmorManager.isWearingPowerArmor(player)}")
			player.sendMessage("Enabled: ${PowerArmorManager.getPowerArmorEnabled(player)}")
			player.sendMessage("Power: ${PowerArmorManager.getArmorPower(player)}")
			player.sendMessage("Weight: ${PowerArmorManager.getCurrentModuleWeight(player)} / ${PowerArmorManager.maxModuleWeight}")

			if (PowerArmorManager.isWearingPowerArmor(player) && PowerArmorManager.getPowerArmorEnabled(player) && PowerArmorManager.getArmorPower(
					player
				) > 0 && PowerArmorManager.getCurrentModuleWeight(player) <= PowerArmorManager.maxModuleWeight
			) {
				player.sendMessage("Enabling modules:")
				PowerArmorManager.getModules(player).forEach { module ->
					module.enableModule(player)
					player.sendMessage("   - ${module.name}")
				}
			} else {
				player.sendMessage("Disabling modules:")
				PowerArmorManager.getModules(player).forEach { module ->
					module.disableModule(player)
					player.sendMessage("   - ${module.name}")
				}
			}
		}
	}
}