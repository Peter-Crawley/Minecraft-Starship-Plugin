package io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin.Companion.plugin
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

abstract class EffectModule: PowerArmorModule() {
	// Represents a power armor module that grants a potion effect
	abstract val effect: PotionEffectType
	abstract val effectMultiplier: Int

	private val period = 5

	private lateinit var task: ApplyPotionEffectTask

	override fun enableModule(player: Player) {
		super.enableModule(player)
		task = ApplyPotionEffectTask(player, effect, effectMultiplier, period)
		task.runTaskTimer(plugin, 0, period.toLong())
	}

	override fun disableModule(player: Player) {
		super.disableModule(player)
		if (this::task.isInitialized) task.cancel()
	}
}

class ApplyPotionEffectTask(private val player: Player, private val effect: PotionEffectType, private val effectMultiplier: Int, private val period: Int) : BukkitRunnable() {
	override fun run() {
		player.addPotionEffect(PotionEffect(effect, period + 1, effectMultiplier, false, false))
	}
}