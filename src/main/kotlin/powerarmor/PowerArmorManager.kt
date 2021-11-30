package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin.Companion.plugin
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.EffectModule
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.PowerArmorModule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType

class PowerArmorManager {
	// Utility functions for dealing with power armor
	// + create power armor itself

	companion object {
		var powerArmorModules = mutableSetOf<PowerArmorModule>()


		// These are all overwritten by the config on init
		var maxPower = 1 // The max power a set can store
		var maxModuleWeight = 1
		var powerItems = mutableMapOf<Material, Int>() // The items that can be placed in the GUI to power the armor

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
	}

	private val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
	private val leggings = ItemStack(Material.LEATHER_LEGGINGS)
	private val boots = ItemStack(Material.LEATHER_BOOTS)
	private val helmet = ItemStack(Material.LEATHER_HELMET)

	init {
		// Get some values from the config
		// TODO: Error handling for missing/bad config values
		maxModuleWeight = plugin.config.getInt("powerArmor.maxModuleWeight")
		maxPower = plugin.config.getInt("powerArmor.maxPower")
		plugin.config.getConfigurationSection("powerArmor.powerItems")!!.getKeys(false).forEach {
			powerItems.putIfAbsent(Material.getMaterial(it)!!, plugin.config.getInt("powerArmor.powerItems.$it"))
		}



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

			// Get the recipe from the config
			// Maybe we can avoid doing this for every single armor piece?
			val recipe = ShapedRecipe(NamespacedKey(plugin, "power-$type"), it)
			recipe.shape(
				*plugin.config.getStringList("powerArmor.recipe.layout").toTypedArray()
			)
			for (craftItemKey in plugin.config.getConfigurationSection(
				"powerArmor.recipe.items"
			)!!.getKeys(false)) {
				// For each key, add key, item to the recipe
				recipe.setIngredient(
					craftItemKey[0],
					Material.getMaterial(plugin.config.getString("powerArmor.recipe.items.$craftItemKey")!!)!!
				)
			}

			recipe.setIngredient('a', it.type)
			Bukkit.addRecipe(recipe)
		}

		// Now that we have the actual power armor items created, load the modules from the config
		plugin.config.getConfigurationSection("powerArmor.modules")!!.getKeys(false).forEach {
			// First, determine whether its a hardcoded module or an effect module
			val type = plugin.config.getString("powerArmor.modules.$it.type")!!
			val newModule: PowerArmorModule
			when (type) {
				"EFFECT" -> {
					// Effect module, load all the stuff from the "effect" config section
					// and create a new module
					newModule = EffectModule(
						ItemStack(Material.getMaterial(plugin.config.getString("powerArmor.modules.$it.item")!!)!!),
						it,
						plugin.config.getString("powerArmor.modules.$it.lore")!!,
						plugin.config.getInt("powerArmor.modules.$it.weight"),
						PotionEffectType.getByName(plugin.config.getString("powerArmor.modules.$it.effect.id")!!)!!,
						plugin.config.getInt("powerArmor.modules.$it.effect.multiplier"),
						plugin.config.getInt("powerArmor.modules.$it.effect.durationBonus"),
						plugin.config.getInt("powerArmor.modules.$it.effect.powerDrain"),
						plugin.config.getInt("powerArmor.modules.$it.effect.period")
					)
				}
				else -> return@forEach // no specified type, move on.
			}

			newModule.createItem()

			// Now parse and add its recipe
			// Maybe in the future create a function for loading recipes from config?
			val recipe =
				ShapedRecipe(NamespacedKey(plugin, "power-module-${newModule.name.replace(" ", "-")}"), newModule.item)
			recipe.shape(
				*plugin.config.getStringList("powerArmor.modules.$it.recipe.layout").toTypedArray()
			)
			for (craftItemKey in plugin.config.getConfigurationSection(
				"powerArmor.modules.$it.recipe.items"
			)!!.getKeys(false)) {
				// For each key, add key, item to the recipe
				recipe.setIngredient(
					craftItemKey[0],
					Material.getMaterial(plugin.config.getString("powerArmor.modules.$it.recipe.items.$craftItemKey")!!)!!
				)
			}
			Bukkit.addRecipe(recipe)
			powerArmorModules.add(newModule)
		}

		// Check once per interval defined in config for players wearing power armor
		ArmorActivatorRunnable().runTaskTimer(plugin, 5, plugin.config.getLong("powerArmor.updateInterval"))
	}
}