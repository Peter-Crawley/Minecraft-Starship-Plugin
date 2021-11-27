package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.powerarmor.PowerArmorManager.Companion.isPowerArmor
import io.github.petercrawley.minecraftstarshipplugin.utils.Screen
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class ModuleScreen(player: Player) : Screen() {
	private val red = ItemStack(Material.RED_STAINED_GLASS)
	private val green = ItemStack(Material.LIME_STAINED_GLASS)

	private val maxWeight = 5 // TODO: load from config
	// The maximum weight in modules a player can use

	init {
		createScreen(player, InventoryType.CHEST, "Power Armor Modules")
		playerEditableSlots.addAll(mutableSetOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 26))

		setAll(mutableSetOf(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25), ItemStack(Material.GRAY_STAINED_GLASS_PANE))


	}

	private fun getCurrentWeight(): Int {
		// Get the combined weight of all of the player's modules
		var weight = 0
		PowerArmorManager.getModules(player).forEach {
			weight += it.weight
		}
		return weight
	}


	fun updateStatusBar() {
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

	override fun onPlayerChangeItem(slot: Int, oldItems: ItemStack?, newItems: ItemStack?) {
		if (isPowerArmor(oldItems) || !isPowerArmor(newItems)) {
			// Player removed a module
		}
		if (isPowerArmor(newItems) || !isPowerArmor(oldItems)) {
			// Player added a module
		}

	}
}