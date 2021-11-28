package io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.potion.PotionEffectType

class NightVisionModule : EffectModule() {
	override val name = "Night Vision Module"
	override val lore = "Grants night vision boost when applied"
	override val effect: PotionEffectType = PotionEffectType.NIGHT_VISION
	override val effectMultiplier = 1
	override val durationBonus = 100 // Don't want night vision to flicker.
	override val weight = 1

	init {
		createItem()
		val recipe = ShapedRecipe(NamespacedKey(MinecraftStarshipPlugin.plugin, "power-module-vision"), item)
		recipe.shape("ooo", "oco", "ooo")
		recipe.setIngredient('o', Material.GOLD_INGOT)
		recipe.setIngredient('c', Material.SPIDER_EYE)
		Bukkit.addRecipe(recipe)
	}
}