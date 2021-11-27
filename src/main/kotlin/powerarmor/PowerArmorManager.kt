package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin.Companion.plugin
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.PowerArmorModule
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

	companion object {
		fun isPowerArmor(armor: ItemStack?): Boolean {
			if (armor == null) return false
			return armor.itemMeta.persistentDataContainer.get(
				NamespacedKey(plugin, "power-module-name"),
				PersistentDataType.STRING
			) != null
		}

		fun isWearingPowerArmor(player: Player): Boolean {
			return isPowerArmor(player.inventory.helmet) &&
					isPowerArmor(player.inventory.chestplate) &&
					isPowerArmor(player.inventory.leggings) &&
					isPowerArmor(player.inventory.boots)
		}

		fun saveModules(player: Player, modules: MutableSet<PowerArmorModule>) {
			// Save the player's current modules to their PersistentDataContainer
			var moduleCSV = "" // Comma separated values of all of the module names

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

		fun getModuleFromItemStack(item: ItemStack): PowerArmorModule? {
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
			TODO()
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
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		// Drop the player's current power armor modules, if keepInventory is off
		if (event.keepInventory) return
		getModules(event.player).forEach {
			event.player.world.dropItem(event.player.location, it.item)
		}
		saveModules(event.player, mutableSetOf<PowerArmorModule>())
	}
}