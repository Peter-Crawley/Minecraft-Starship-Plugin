package io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules

import org.bukkit.potion.PotionEffectType

abstract class EffectModule: PowerArmorModule() {
	// Represents a power armor module that grants a potion effect
	abstract val effect: PotionEffectType

}