package io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

abstract class PowerArmorModule {
	var item = ItemStack(Material.FLINT)

	fun createItem(name: String, lore: String, recipe: ShapedRecipe) {
		val meta = item.itemMeta
		val itemLore: MutableList<Component> = ArrayList()
		itemLore.add(Component.text(lore, NamedTextColor.DARK_GREEN))
		meta.lore(itemLore)

		meta.displayName(Component.text(name, NamedTextColor.GOLD))

		meta.persistentDataContainer.set(
			NamespacedKey(MinecraftStarshipPlugin.plugin,"is-power-module"),
			PersistentDataType.INTEGER, 1
		)
		meta.persistentDataContainer.set(
			NamespacedKey(MinecraftStarshipPlugin.plugin,"power-module-${name.replace(" ", "-")}"),
			PersistentDataType.INTEGER, 1
		)

		item.itemMeta = meta
		Bukkit.addRecipe(recipe)

	}
	open fun enableModule() {}
	open fun disableModule() {}

}