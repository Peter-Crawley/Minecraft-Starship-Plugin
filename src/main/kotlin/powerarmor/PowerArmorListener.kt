package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.PowerArmorModule
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent

class PowerArmorListener : Listener {
	@EventHandler
	fun onPlayerInteractEvent(event: PlayerInteractEvent) {
		if (PowerArmorManager.isPowerArmor(event.item)) {
			ModuleScreen(event.player)
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		// Drop the player's current power armor modules, if keepInventory is off
		if (event.keepInventory) return
		val playerManager = PlayerArmorManager(event.entity)
		playerManager.modules.forEach {
			event.entity.world.dropItem(event.entity.location, it.item)
		}
		playerManager.modules = mutableSetOf<PowerArmorModule>()
	}
}