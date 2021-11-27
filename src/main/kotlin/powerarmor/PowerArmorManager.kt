package io.github.petercrawley.minecraftstarshipplugin.powerarmor

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin.Companion.plugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.persistence.PersistentDataType

class PowerArmorManager {

	companion object{
		@JvmStatic // just in case
		fun isPowerArmor(armor: ItemStack?): Boolean{
			if (armor == null) return false;
			return armor.itemMeta.persistentDataContainer.get(NamespacedKey(plugin,"is-power-armor"), PersistentDataType.INTEGER) != null
		}

		@JvmStatic
		fun isWearingPowerArmor(player: Player): Boolean{
			return isPowerArmor(player.inventory.helmet) &&
					isPowerArmor(player.inventory.chestplate) &&
					isPowerArmor(player.inventory.leggings) &&
					isPowerArmor(player.inventory.boots)
		}
	}

	val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
	val leggings = ItemStack(Material.LEATHER_LEGGINGS)
	val boots = ItemStack(Material.LEATHER_BOOTS)
	val helmet = ItemStack(Material.LEATHER_HELMET)

	init {
		mutableSetOf<ItemStack>(helmet,chestplate,leggings,boots).forEach{
			val meta = it.itemMeta as LeatherArmorMeta
			val lore: MutableList<Component> = ArrayList()
			lore.add(Component.text("Modules can be added to a full set", NamedTextColor.DARK_GREEN))
			meta.lore(lore)

			// I'm not going to say I like this logic, but it works.
			// This bothers me, I really, really want to use capitalize()
			val type = it.type.toString().split("_")[1].lowercase().replaceFirstChar { char -> char.titlecase() }
			meta.displayName(Component.text("Power $type", NamedTextColor.GOLD))

			meta.persistentDataContainer.set(NamespacedKey(plugin,"is-power-armor"),PersistentDataType.INTEGER, 1)
			it.itemMeta = meta

			val recipe = ShapedRecipe(NamespacedKey(plugin, "power-$type"), it)
			recipe.shape("ooo","oco","ooo") // Might want to get the recipe from a config?
			recipe.setIngredient('o',Material.IRON_INGOT) // Temporary Recipe
			recipe.setIngredient('c',it.type)
			Bukkit.addRecipe(recipe)
		}
	}
}