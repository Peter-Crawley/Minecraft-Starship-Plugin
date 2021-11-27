package io.github.petercrawley.minecraftstarshipplugin.utils

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin.Companion.plugin
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

abstract class Screen : Listener {
	lateinit var player: Player
		private set

	lateinit var screen: Inventory
		private set

	private fun createScreen(player: Player, inventory: Inventory) {
		this.player = player
		this.screen = inventory
		onScreenUpdate()
		Bukkit.getPluginManager().registerEvents(this, plugin)
		player.openInventory(screen)
	}

	fun createScreen(player: Player, type: InventoryType, name: String) {
		createScreen(player, Bukkit.createInventory(player, type, text(name)))
	}

	fun createScreen(player: Player, size: Int, name: String) {
		createScreen(player, Bukkit.createInventory(player, size, text(name)))
	}

	open fun onScreenUpdate() {}

	open fun onScreenButtonClicked(slot: Int) {}

	open fun onPlayerPlaceItem(slot: Int, items: ItemStack) {}

	open fun onScreenClosed() {}

	fun closeScreen() {
		// Unregister handlers first, otherwise we will create a loop when we call screen.close()
		InventoryCloseEvent.getHandlerList().unregister(this)
		InventoryDragEvent.getHandlerList().unregister(this)
		InventoryClickEvent.getHandlerList().unregister(this)

		screen.close()

		onScreenClosed()
	}

	@EventHandler
	fun onInventoryClickEvent(event: InventoryClickEvent) {
		if (event.inventory != screen) return
		event.isCancelled = true
		onScreenButtonClicked(event.rawSlot)
		onScreenUpdate()
	}

	@EventHandler
	fun onPlayerMoveItemToInventoryEvent(event: InventoryDragEvent) {
		if (event.inventory != screen) return;
		// It doesn't look like Paper will tell us which slot had what added to it, so we just cancel
		// anything that involves more than one slot.
		if (event.inventorySlots.size > 1) {
			event.isCancelled = true
			return
		}
		onPlayerPlaceItem(event.inventorySlots.first(), event.newItems[0]!!)
		onScreenUpdate()
	}

	@EventHandler
	fun onPlayerCloseScreenEvent(event: InventoryCloseEvent) {
		if (event.inventory == screen) closeScreen()
	}
}