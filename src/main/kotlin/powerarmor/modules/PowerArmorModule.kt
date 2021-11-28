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
	abstract val name: String
	abstract val lore: String
	abstract val recipe: ShapedRecipe
	abstract val weight: Int

	open fun createItem() {
		val meta = item.itemMeta
		val itemLore: MutableList<Component> = ArrayList()
		itemLore.add(Component.text(lore, NamedTextColor.DARK_GREEN))
		meta.lore(itemLore)

		meta.displayName(Component.text(name, NamedTextColor.GOLD))

		meta.persistentDataContainer.set(
			NamespacedKey(MinecraftStarshipPlugin.plugin, "power-module-name"),
			PersistentDataType.STRING,
			name
		)

		item.itemMeta = meta
		Bukkit.addRecipe(recipe)

	}

	open fun enableModule() {}
	open fun disableModule() {}

}