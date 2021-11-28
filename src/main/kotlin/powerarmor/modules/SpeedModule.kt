package io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.potion.PotionEffectType

class SpeedModule : EffectModule() {
	override val name = "Speed Module"
	override val lore = "Grants speed when applied"
	override val effect: PotionEffectType = PotionEffectType.SPEED
	override val weight = 2
	override val recipe: ShapedRecipe =
		ShapedRecipe(NamespacedKey(MinecraftStarshipPlugin.plugin, "power-module-$name"), item)

	init {
		recipe.shape("ooo", "oco", "ooo")
		recipe.setIngredient('o', Material.GOLD_INGOT)
		recipe.setIngredient('c', Material.SUGAR)

		createItem()
	}
}