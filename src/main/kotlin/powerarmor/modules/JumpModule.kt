package io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.potion.PotionEffectType

class JumpModule : EffectModule() {
	override val name = "Jump Module"
	override val lore = "Grants jump boost when applied"
	override val effect: PotionEffectType = PotionEffectType.JUMP
	override val effectMultiplier =1
	override val weight = 2

	init {
		createItem()
		val recipe = ShapedRecipe(NamespacedKey(MinecraftStarshipPlugin.plugin, "power-module-jump"), item)
		recipe.shape("ooo", "oco", "ooo")
		recipe.setIngredient('o', Material.GOLD_INGOT)
		recipe.setIngredient('c', Material.RABBIT_FOOT)
		Bukkit.addRecipe(recipe)
	}
}