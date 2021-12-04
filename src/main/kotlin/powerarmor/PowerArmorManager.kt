package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin.Companion.plugin
import io.github.petercrawley.minecraftstarshipplugin.events.MSPConfigReloadEvent
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.EffectModule
import io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules.PowerArmorModule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType

class PowerArmorManager : Listener {
	// Utility functions for dealing with power armor
	// + create power armor itself from the config file

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

	private lateinit var chestplate: ItemStack
	private lateinit var leggings: ItemStack
	private lateinit var boots: ItemStack
	private lateinit var helmet: ItemStack

	private lateinit var runnable: ArmorActivatorRunnable

	init {
		plugin.server.pluginManager.registerEvents(this, plugin)
		reloadPowerArmor()
	}

	private fun loadRecipe(key: NamespacedKey, item: ItemStack, path: String, items: Map<Char, Material> = mapOf()) {
		// Load and register the recipe in the config at path
		// Items represents any characters that have a predefined item
		// Recipe format:
		// <path> :
		// 		layout:
		// 			- "ccc"
		// 			- "ccc"
		// 			- "ccc"
		// 		items:
		//			c: SEA_PICKLE

		// If an old recipe exists with this key, remove it
		plugin.server.removeRecipe(key)

		val recipe = ShapedRecipe(key, item)
		recipe.shape(
			*plugin.config.getStringList("$path.layout").toTypedArray()
		)
		for (craftItemKey in plugin.config.getConfigurationSection(
			"$path.items"
		)!!.getKeys(false)) {
			// For each key, add key, item to the recipe
			recipe.setIngredient(
				craftItemKey[0],
				Material.getMaterial(plugin.config.getString("$path.items.$craftItemKey")!!)!!
			)
		}
		items.forEach { (c, m) ->
			recipe.setIngredient(c, m)
		}
		Bukkit.addRecipe(recipe)
	}

	private fun loadModules() {
		// Load the modules from the config
		powerArmorModules = mutableSetOf() // clear the modules

		plugin.config.getConfigurationSection("powerArmor.modules")!!.getKeys(false).forEach {
			// First, determine whether its a hardcoded module or an effect module
			val type = plugin.config.getString("powerArmor.modules.$it.type")!!
			val newModule: PowerArmorModule
			when (type) {
				"EFFECT" -> {
					// Effect module, load all the stuff from the "effect" config section
					// and create a new module
					newModule = EffectModule(
						ItemStack(Material.getMaterial(plugin.config.getString("powerArmor.modules.$it.material")!!)!!),
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
				else -> {
					plugin.logger.warning("Unknown module type: $type")
					return@forEach // no specified type, move on.
				}
			}

			newModule.createItem()
			loadRecipe(
				NamespacedKey(plugin, "power-module-${newModule.name.replace(" ", "-")}"),
				newModule.item,
				"powerArmor.modules.$it.recipe"
			)
			powerArmorModules.add(newModule)
		}
	}


	private fun loadArmor() {
		// Reset the armor, and load it from the config
		chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
		leggings = ItemStack(Material.LEATHER_LEGGINGS)
		boots = ItemStack(Material.LEATHER_BOOTS)
		helmet = ItemStack(Material.LEATHER_HELMET)

		mutableSetOf(helmet, chestplate, leggings, boots).forEach {
			val meta = it.itemMeta as LeatherArmorMeta
			val lore: MutableList<Component> = ArrayList()
			lore.add(Component.text(plugin.config.getString("powerArmor.lore")!!, NamedTextColor.DARK_GREEN))
			meta.lore(lore)

			// I'm not going to say I like this logic, but it works.
			// This bothers me, I really, really want to use capitalize()
			val typeName = it.type.toString().split("_")[1].lowercase().replaceFirstChar { char -> char.titlecase() }
			meta.displayName(Component.text("Power $typeName", NamedTextColor.GOLD))

			meta.persistentDataContainer.set(NamespacedKey(plugin, "is-power-armor"), PersistentDataType.INTEGER, 1)
			it.itemMeta = meta
			loadRecipe(
				NamespacedKey(plugin, "power-$typeName"),
				it,
				"powerArmor.recipe",
				mutableMapOf("a"[0] to it.type)
			)
		}
	}


	private fun reloadPowerArmor() {
		// Reset everything and load it from the config again
		powerItems = mutableMapOf() // clear the power items
		if (this::runnable.isInitialized) runnable.cancel() // cancel the runnable, the interval might have changed

		// Load some base values
		maxModuleWeight = plugin.config.getInt("powerArmor.maxModuleWeight")
		maxPower = plugin.config.getInt("powerArmor.maxPower")
		plugin.config.getConfigurationSection("powerArmor.powerItems")!!.getKeys(false).forEach {
			powerItems.putIfAbsent(Material.getMaterial(it)!!, plugin.config.getInt("powerArmor.powerItems.$it"))
		}

		loadArmor()
		loadModules()

		// Check once per interval defined in config for players wearing power armor
		ArmorActivatorRunnable().runTaskTimer(plugin, 5, plugin.config.getLong("powerArmor.updateInterval"))
	}


	@EventHandler
	fun onPlayerInteractEvent(event: PlayerInteractEvent) {
		// Bring up the power armor menu
		if (isPowerArmor(event.item)) {
			ModuleScreen(event.player)
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		// Drop the player's current power armor modules, if keepInventory is off
		if (event.keepInventory) return
		val playerArmor = PlayerPowerArmor(event.entity)
		playerArmor.modules.forEach {
			event.entity.world.dropItem(event.entity.location, it.item)
		}
		playerArmor.modules = mutableSetOf<PowerArmorModule>()
		// Remove armor power
		playerArmor.armorPower = 0
	}

	@EventHandler
	fun onConfigReload(event: MSPConfigReloadEvent) {
		reloadPowerArmor()
	}
}