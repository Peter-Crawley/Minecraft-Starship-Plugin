package io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.potion.PotionEffectType

class SpeedModule : EffectModule() {
	override val name = "Speed Module"
	override val lore = "Grants speed when applied"
	override val effect: PotionEffectType = PotionEffectType.SPEED
	override val effectMultiplier = 1
	override val weight = 3

	init {
		createItem()
		val recipe = ShapedRecipe(NamespacedKey(MinecraftStarshipPlugin.plugin, "power-module-speed"), item)
		recipe.shape("ooo", "oco", "ooo")
		recipe.setIngredient('o', Material.GOLD_INGOT)
		recipe.setIngredient('c', Material.SUGAR)
		Bukkit.addRecipe(recipe)
	}
}