package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.powerarmor.PowerArmorManager.Companion.getModuleFromItemStack
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.PowerArmorModule
import io.github.petercrawley.minecraftstarshipplugin.utils.Screen
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class ModuleScreen(player: Player) : Screen() {
	private val red = ItemStack(Material.RED_STAINED_GLASS)
	private val green = ItemStack(Material.LIME_STAINED_GLASS)

	init {
		createScreen(player, InventoryType.CHEST, "Power Armor Modules")
		playerEditableSlots.addAll(mutableSetOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 26))

		setAll(mutableSetOf(5, 6, 7, 14, 15, 16, 17, 23, 24, 25), ItemStack(Material.GRAY_STAINED_GLASS_PANE))
		updateStatus()

		// Put instances of every module they have in the slots
		val slots = arrayOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
		var index = 0
		PowerArmorManager.getModules(player).forEach {
			screen.setItem(slots[index], it.item)
			index++
		}
		// Insert the toggle button
		val enabled = PowerArmorManager.getPowerArmorEnabled(player)
		updateToggleButton(enabled)

		// Clear their modules, they get added back on screen close
		PowerArmorManager.setModules(player, mutableSetOf<PowerArmorModule>())
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
		val color = ItemStack(if (weight <= PowerArmorManager.maxModuleWeight) {
			green
		} else {
			red
		})
		// Name it
		val colorMeta = color.itemMeta
		colorMeta.displayName(Component.text("Weight: ${PowerArmorManager.getCurrentModuleWeight(player)} / ${PowerArmorManager.maxModuleWeight}"))
		color.itemMeta = colorMeta
		// Set it
		setAll(mutableSetOf(4, 13, 22), color)

		// Update the power indicator
		val power = PowerArmorManager.getArmorPower(player)
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
				val enabled = !PowerArmorManager.getPowerArmorEnabled(player)
				updateToggleButton(enabled)
				PowerArmorManager.setPowerArmorEnabled(player, enabled)
			}
		}
	}

	override fun onScreenClosed() {
		// Save every module to the player
		val slots = arrayOf(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
		slots.forEach {
			val module = getModuleFromItemStack(screen.getItem(it))
			if (module != null) PowerArmorManager.addModule(player, module)
		}
	}

	override fun onPlayerChangeItem(slot: Int, oldItems: ItemStack?, newItems: ItemStack?) {
		if (slot == 26 && newItems != null) {
			// Player added fuel to the power slot
			if (PowerArmorManager.powerItems.containsKey(newItems.type)) {
				for (i in 0..newItems.amount) {
					val currentPower = PowerArmorManager.getArmorPower(player)
					if (currentPower < PowerArmorManager.maxPower) {
						PowerArmorManager.addArmorPower(player, PowerArmorManager.powerItems[newItems.type])
						newItems.amount--
					}
				}
			}
		}
	}
}