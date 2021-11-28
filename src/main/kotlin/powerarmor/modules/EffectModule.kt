package io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin.Companion.plugin
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

abstract class EffectModule : PowerArmorModule() {
	// Represents a power armor module that grants a potion effect
	abstract val effect: PotionEffectType
	abstract val effectMultiplier: Int

	private val period = 5
	private val players = mutableMapOf<UUID,ApplyPotionEffectTask>()

	override fun enableModule(player: Player) {
		super.enableModule(player)
		val task = ApplyPotionEffectTask(player, effect, effectMultiplier, period)
		task.runTaskTimer(plugin, 0, period.toLong())
		players.putIfAbsent(player.uniqueId, task)
	}

	override fun disableModule(player: Player) {
		super.disableModule(player)
		val task = players[player.uniqueId] ?: return
		task.cancel()
	}
}

class ApplyPotionEffectTask(
	private val player: Player,
	private val effect: PotionEffectType,
	private val effectMultiplier: Int,
	private val period: Int
) : BukkitRunnable() {
	override fun run() {
		player.addPotionEffect(PotionEffect(effect, period + 1, effectMultiplier, false, false))
	}
}