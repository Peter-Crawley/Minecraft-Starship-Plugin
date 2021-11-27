package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class ModuleScreenListener : Listener {
	@EventHandler
	fun onPlayerInteractEvent(event: PlayerInteractEvent) {
		if (event.item != null) {
			if (PowerArmorManager.isPowerArmor(event.item)) {
				ModuleScreen(event.player)
				event.isCancelled = true
			}
		}
	}
}