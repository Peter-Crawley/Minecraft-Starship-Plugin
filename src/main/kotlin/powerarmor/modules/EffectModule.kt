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
	open val durationBonus = 1
	open val powerDrain = 1 // The amount drained every period ticks

	val period = 5 // How often the potion effect is re-applied
	private val players = mutableMapOf<UUID, ApplyPotionEffectTask>()

	override fun enableModule(player: Player) {
		if (players.containsKey(player.uniqueId)) return // already activated
		super.enableModule(player)
		val task = ApplyPotionEffectTask(player, this)
		task.runTaskTimer(plugin, 0, period.toLong())
		players.putIfAbsent(player.uniqueId, task)
	}

	override fun disableModule(player: Player) {
		if (!players.containsKey(player.uniqueId)) return // already deactivated
		super.disableModule(player)
		val task = players[player.uniqueId] ?: return
		task.cancel()
		players.remove(player.uniqueId)
	}
}

class ApplyPotionEffectTask(
	private val player: Player,
	private val module: EffectModule

) : BukkitRunnable() {
	override fun run() {
		module.drainPower(player, module.powerDrain)
		player.addPotionEffect(
			PotionEffect(
				module.effect,
				module.period + module.durationBonus + 1, // 1 for buffer
				module.effectMultiplier,
				false,
				false
			)
		)
	}
}