package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.utils.NamedItem
import io.github.petercrawley.minecraftstarshipplugin.utils.Screen
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class ModuleScreen(player: Player) : Screen() {
	init {
		createScreen(player, InventoryType.CHEST, "Power Armor Modules")

	}

	override fun onScreenButtonClicked(slot: Int) {
		when (slot) {
			0 -> TODO()
		}
	}

	override fun onPlayerPlaceItem(slot: Int, items: ItemStack) {
		TODO()
	}
}