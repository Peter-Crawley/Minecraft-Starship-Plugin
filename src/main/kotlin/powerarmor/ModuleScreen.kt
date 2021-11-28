package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.powerarmor.PowerArmorManager.Companion.getModuleFromItemStack
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.PowerArmorManager.Companion.isPowerModule
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.PowerArmorModule
import io.github.petercrawley.minecraftstarshipplugin.utils.Screen
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class ModuleScreen(player: Player) : Screen() {
	private val red = ItemStack(Material.RED_STAINED_GLASS)
	private val green = ItemStack(Material.LIME_STAINED_GLASS)

	private val maxWeight = 4 // TODO: load from config
	// The maximum weight in modules a player can use

	init {
		createScreen(player, InventoryType.CHEST, "Power Armor Modules")
		playerEditableSlots.addAll(mutableSetOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 26))

		setAll(mutableSetOf(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25), ItemStack(Material.GRAY_STAINED_GLASS_PANE))
		updateStatusBar()

		// Put instances of every module they have in the slots
		val slots = arrayOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
		var index = 0
		PowerArmorManager.getModules(player).forEach {
			screen.setItem(slots[index], it.item)
			index++
		}
	}

	private fun getCurrentWeight(): Int {
		// Get the combined weight of all of the player's modules
		var weight = 0
		PowerArmorManager.getModules(player).forEach {
			weight += it.weight
		}
		return weight
	}


	private fun updateStatusBar() {
		// Update the colored status bar in the middle, that tells the weight status
		if (getCurrentWeight() <= maxWeight) {
			setAll(mutableSetOf(4, 13, 22), green)
		} else {
			setAll(mutableSetOf(4, 13, 22), red)
		}
	}

	override fun onScreenUpdate() {
		updateStatusBar()
	}

	override fun onScreenClosed() {
		// Save every module to the player
		val slots = arrayOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
		val modules = mutableSetOf<PowerArmorModule>()
		slots.forEach {
			val module = getModuleFromItemStack(screen.getItem(it))
			if (module != null) modules.add(module)
		}
		PowerArmorManager.saveModules(player, modules)
	}

	override fun onPlayerChangeItem(slot: Int, oldItems: ItemStack?, newItems: ItemStack?) {
		if (isPowerModule(oldItems) || !isPowerModule(newItems)) {
			// Player removed a module
			getModuleFromItemStack(oldItems)?.disableModule(player)

		}
		if (isPowerModule(newItems) || !isPowerModule(oldItems)) {
			// Player added a module
			getModuleFromItemStack(newItems)?.enableModule(player)
		}
	}
}