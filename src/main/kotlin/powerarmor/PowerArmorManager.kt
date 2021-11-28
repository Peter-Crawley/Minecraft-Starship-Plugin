package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin.Companion.plugin
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.JumpModule
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.NightVisionModule
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.PowerArmorModule
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.SpeedModule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.persistence.PersistentDataType

class PowerArmorManager : Listener {
	// Utility functions for dealing with power armor
	// + create power armor iteself

	companion object {
		var powerArmorModules = mutableSetOf<PowerArmorModule>(SpeedModule(), JumpModule(), NightVisionModule())
		val maxPower = 1000 // The max power a set can store

		// The items that can be placed in the GUI to power the armor
		val powerItems = mutableMapOf<Material, Int>(Material.COAL to 10)

		fun isPowerArmor(armor: ItemStack?): Boolean {
			if (armor == null) return false
			return armor.itemMeta.persistentDataContainer.get(
				NamespacedKey(plugin, "is-power-armor"),
				PersistentDataType.INTEGER
			) != null
		}

		fun isPowerModule(module: ItemStack?): Boolean {
			if (module == null) return false
			return module.itemMeta.persistentDataContainer.get(
				NamespacedKey(plugin, "power-module-name"),
				PersistentDataType.STRING
			) != null
		}

		fun isWearingPowerArmor(player: Player): Boolean {
			return (isPowerArmor(player.inventory.helmet) &&
					isPowerArmor(player.inventory.chestplate) &&
					isPowerArmor(player.inventory.leggings) &&
					isPowerArmor(player.inventory.boots))
		}

		fun saveModules(player: Player, modules: MutableSet<PowerArmorModule>) {
			// Save the player's current modules to their PersistentDataContainer
			var moduleCSV = "" // Comma separated values of all the module names

			modules.forEach {
				moduleCSV += it.name + ","
			}
			player.persistentDataContainer.set(
				NamespacedKey(plugin, "equipped-power-armor-modules"),
				PersistentDataType.STRING,
				moduleCSV
			)
		}

		fun getModules(player: Player): MutableSet<PowerArmorModule> {
			// Get the player's current modules from their PersistentDataContainer
			val moduleCSV = player.persistentDataContainer.get(
				NamespacedKey(plugin, "equipped-power-armor-modules"),
				PersistentDataType.STRING
			)
				?: return mutableSetOf<PowerArmorModule>()
			val moduleNames = moduleCSV.split(",")
			val modules = mutableSetOf<PowerArmorModule>()
			moduleNames.forEach {
				val module = getModuleFromName(it)
				if (module != null) modules.add(module)
			}
			return modules
		}

		fun getModuleFromItemStack(item: ItemStack?): PowerArmorModule? {
			if (item == null) return null
			return getModuleFromName(
				item.itemMeta.persistentDataContainer.get(
					NamespacedKey(
						plugin,
						"power-module-name"
					), PersistentDataType.STRING
				)
			)
		}

		fun getModuleFromName(name: String?): PowerArmorModule? {
			powerArmorModules.forEach {
				if (it.name == name) {
					return it
				}
			}
			return null
		}

		fun getArmorPower(player: Player): Int {
			return player.persistentDataContainer.get(NamespacedKey(plugin, "power-armor-power"), PersistentDataType.INTEGER)
					?: return 0
		}

		fun setArmorPower(player: Player, power: Int) {
			var newPower = power
			if (newPower > maxPower) newPower = maxPower
			player.persistentDataContainer.set(NamespacedKey(plugin, "power-armor-power"), PersistentDataType.INTEGER, newPower)
		}

		fun addArmorPower(player: Player, power: Int?) {
			if (power == null) return
			setArmorPower(player, getArmorPower(player) + power)
		}
	}

	private val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
	private val leggings = ItemStack(Material.LEATHER_LEGGINGS)
	private val boots = ItemStack(Material.LEATHER_BOOTS)
	private val helmet = ItemStack(Material.LEATHER_HELMET)

	init {
		plugin.server.pluginManager.registerEvents(this, plugin)

		mutableSetOf(helmet, chestplate, leggings, boots).forEach {
			val meta = it.itemMeta as LeatherArmorMeta
			val lore: MutableList<Component> = ArrayList()
			lore.add(Component.text("Modules can be added to a full set", NamedTextColor.DARK_GREEN))
			meta.lore(lore)

			// I'm not going to say I like this logic, but it works.
			// This bothers me, I really, really want to use capitalize()
			val type = it.type.toString().split("_")[1].lowercase().replaceFirstChar { char -> char.titlecase() }
			meta.displayName(Component.text("Power $type", NamedTextColor.GOLD))

			meta.persistentDataContainer.set(NamespacedKey(plugin, "is-power-armor"), PersistentDataType.INTEGER, 1)
			it.itemMeta = meta

			val recipe = ShapedRecipe(NamespacedKey(plugin, "power-$type"), it)
			recipe.shape("ooo", "oco", "ooo") // Might want to get the recipe from a config?
			recipe.setIngredient('o', Material.IRON_INGOT) // Temporary Recipe
			recipe.setIngredient('c', it.type)
			Bukkit.addRecipe(recipe)
		}

		// Check once per second for players wearing power armor
		ArmorActivatorRunnable().runTaskTimer(plugin, 5, 20)
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		// Drop the player's current power armor modules, if keepInventory is off
		if (event.keepInventory) return
		getModules(event.entity).forEach {
			event.entity.world.dropItem(event.entity.location, it.item)
		}
		saveModules(event.entity, mutableSetOf<PowerArmorModule>())
	}
}