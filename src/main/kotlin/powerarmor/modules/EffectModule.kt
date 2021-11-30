package io.github.petercrawley.minecraftstarshipplugin.powerarmor.modules

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin.Companion.plugin
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

class EffectModule(
	override val item: ItemStack,
	override val name: String,
	override val lore: String,
	override val weight: Int,
	val effect: PotionEffectType,
	val effectMultiplier: Int,
	val durationBonus: Int,
	val powerDrain: Int,
	val period: Int
) : PowerArmorModule() {
	// Represents a power armor module that grants a potion effect to the player.

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
	// Need to periodically re-apply the module's potion effect on the player.
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