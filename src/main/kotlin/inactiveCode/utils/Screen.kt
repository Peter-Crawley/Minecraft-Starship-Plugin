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
import org.bukkit.scheduler.BukkitRunnable


abstract class Screen : Listener {
	lateinit var player: Player
		private set

	lateinit var screen: Inventory
		private set

	// The slots in which the player can place/remove items
	val playerEditableSlots = mutableSetOf<Int>()

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

	open fun onPlayerChangeItem(slot: Int, oldItems: ItemStack?, newItems: ItemStack?) {}

	open fun onScreenClosed() {}

	fun closeScreen() {
		// Unregister handlers first, otherwise we will create a loop when we call screen.close()
		InventoryCloseEvent.getHandlerList().unregister(this)
		InventoryDragEvent.getHandlerList().unregister(this)
		InventoryClickEvent.getHandlerList().unregister(this)

		screen.close()

		onScreenClosed()
	}

	fun setAll(slots: MutableSet<Int>, item: ItemStack){
		slots.forEach{
			screen.setItem(it, item)
		}
	}

	@EventHandler
	fun onInventoryClickEvent(event: InventoryClickEvent) {
		if (event.clickedInventory != screen) {
			// Might as well still update screen
			onScreenUpdate()
			return
		}
		if (playerEditableSlots.contains(event.rawSlot)) {
			// Player editable slot
			// In one server tick (once the item transfer takes place) trigger any actions based on the old slot contents
			// and the new slot contents. Honestly, we don't care about the player's cursor.
			PlayerMoveItemTask(event.slot, this, event.currentItem).runTaskLater(plugin, 1)
			// It calls onScreenUpdate() later
		}
		else{
			// Not a player-editable slot, it's probably a button
			event.isCancelled = true
			onScreenButtonClicked(event.rawSlot)
			onScreenUpdate()
		}
	}

	@EventHandler
	fun onPlayerDragItemEvent(event: InventoryDragEvent) {
		if (event.inventory == screen) {
			event.isCancelled = true
			// Might as well update screen
			onScreenUpdate()
		};
	}

	@EventHandler
	fun onPlayerCloseScreenEvent(event: InventoryCloseEvent) {
		if (event.inventory == screen) closeScreen()
	}
}

class PlayerMoveItemTask(private val slot: Int, private val screen: Screen, private val oldItems: ItemStack?) : BukkitRunnable() {
	// Exists so we can compare what was in the inventory slot before the InventoryClickEvent with what is in it a tick later.
	// You could probably figure this out by getting the InventoryAction and comparing the slot contents before with the
	// cursor contents before, but this is easier.
	override fun run() {
		screen.onPlayerChangeItem(slot, oldItems, screen.screen.getItem(slot))
		screen.onScreenUpdate()
	}
}