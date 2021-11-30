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
			player.sendMessage("\n ".repeat(20))
			player.sendMessage("--------Power Armor Status--------\n ")
			player.sendMessage("Power Items ${PowerArmorManager.powerItems}")
			player.sendMessage("Power: ${playerManager.armorPower} / ${PowerArmorManager.maxPower}")
			player.sendMessage("Weight: ${playerManager.moduleWeight} / ${PowerArmorManager.maxModuleWeight}")
			player.sendMessage("Enabled: ${playerManager.armorEnabled}")
			player.sendMessage("Wearing: ${playerManager.wearingPowerArmor}")
			if (playerManager.wearingPowerArmor && playerManager.armorEnabled && playerManager.armorPower > 0 && playerManager.moduleWeight <= PowerArmorManager.maxModuleWeight) {
				player.sendMessage("\nEnabling Modules:")
				playerManager.modules.forEach { module ->
					module.enableModule(player)
					player.sendMessage("   - ${module.name}")
				}
			} else {
				player.sendMessage("\nDisabling Modules:")
				playerManager.modules.forEach { module ->
					module.disableModule(player)
					player.sendMessage("   - ${module.name}")
				}
			}
		}
	}
}