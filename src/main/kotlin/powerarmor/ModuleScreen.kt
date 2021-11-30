package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.powerarmor.PowerArmorManager.Companion.getModuleFromItemStack
import io.github.petercrawley.minecraftstarshipplugin.utils.Screen
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class ModuleScreen(player: Player) : Screen() {
	private val red = ItemStack(Material.RED_STAINED_GLASS)
	private val green = ItemStack(Material.LIME_STAINED_GLASS)
	private val playerManager = PlayerArmorManager(player)

	init {
		createScreen(player, InventoryType.CHEST, "Power Armor Modules")
		playerEditableSlots.addAll(mutableSetOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 26))

		setAll(mutableSetOf(5, 6, 7, 14, 15, 16, 17, 23, 24, 25), ItemStack(Material.GRAY_STAINED_GLASS_PANE))

		// Put instances of every module they have in the slots
		val slots = arrayOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
		var index = 0
		playerManager.modules.forEach {
			screen.setItem(slots[index], it.item)
			playerManager.removeModule(it)
			index++
		}
		// Insert the toggle button
		updateToggleButton(playerManager.armorEnabled)

		// Clear their modules, they get added back on screen close
		// Don't just set it to empty or the modules won't disable
		updateStatus()
	}

	private fun updateStatus() {
		// Update the colored status bar in the middle, that tells the weight status
		// Since we temporarily removed all of their modules, we can't use PowerArmorManager.getCurrentModuleWeight()
		val slots = arrayOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
		var weight = 0
		slots.forEach {
			val module = getModuleFromItemStack(screen.getItem(it))
			if (module != null) weight += module.weight
		}
		// Figure out what color to make the status bar
		val color = ItemStack(
			if (weight <= PowerArmorManager.maxModuleWeight) {
				green
			} else {
				red
			}
		)
		// Name it
		val colorMeta = color.itemMeta
		colorMeta.displayName(Component.text("Weight: ${weight} / ${PowerArmorManager.maxModuleWeight}"))
		color.itemMeta = colorMeta
		// Set it
		setAll(mutableSetOf(4, 13, 22), color)

		// Update the power indicator
		val power = playerManager.armorPower
		val item = ItemStack(
			when {
				power >= PowerArmorManager.maxPower -> Material.BLUE_STAINED_GLASS
				power >= (PowerArmorManager.maxPower / 4) * 3 -> Material.GREEN_STAINED_GLASS
				power >= PowerArmorManager.maxPower / 2 -> Material.LIME_STAINED_GLASS
				power >= PowerArmorManager.maxPower / 4 -> Material.YELLOW_STAINED_GLASS
				power > 0 -> Material.ORANGE_STAINED_GLASS
				else -> Material.RED_STAINED_GLASS
			}
		)
		val meta = item.itemMeta
		meta.displayName(Component.text("Power: $power/${PowerArmorManager.maxPower}"))
		item.itemMeta = meta
		screen.setItem(17, item)
	}


	fun updateToggleButton(enabled: Boolean) {
		var item = ItemStack(Material.RED_STAINED_GLASS)
		if (enabled) item = ItemStack(Material.LIME_STAINED_GLASS)
		val meta = item.itemMeta
		meta.displayName(Component.text(if (enabled) "Enabled" else "Disabled"))
		item.itemMeta = meta
		screen.setItem(8, item)
	}

	override fun onScreenUpdate() {
		updateStatus()
	}

	override fun onScreenButtonClicked(slot: Int) {
		when (slot) {
			8 -> {
				playerManager.armorEnabled = !playerManager.armorEnabled
				updateToggleButton(playerManager.armorEnabled)
			}
		}
	}

	override fun onScreenClosed() {
		// Save every module to the player
		val slots = playerEditableSlots
		slots.forEach {
			val module = getModuleFromItemStack(screen.getItem(it))
			if (module != null) {
				if (!playerManager.modules.contains(module)) playerManager.addModule(module)
				else player.inventory.addItem(module.item)
			}
			else {
				// It's not a module but we should still give it back to them
				if (screen.getItem(it) != null) player.inventory.addItem(screen.getItem(it)!!)
			}
		}
	}

	override fun onPlayerChangeItem(slot: Int, oldItems: ItemStack?, newItems: ItemStack?) {
		if (slot == 26 && newItems != null) {
			// Player added fuel to the power slot
			if (PowerArmorManager.powerItems.containsKey(newItems.type)) {
				for (i in 0..newItems.amount) {
					val currentPower = playerManager.armorPower
					if (currentPower < PowerArmorManager.maxPower) {
						playerManager.armorPower = currentPower + PowerArmorManager.powerItems[newItems.type]!!
						newItems.amount--
					}
				}
			}
		}
	}
}