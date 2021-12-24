package de.lookonthebrightsi.arena

import de.hglabor.utils.kutils.playSound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.Vector

fun Player.checkMechanics(blockBelow: Block) {
    when (blockBelow.type) {
        Material.SLIME_BLOCK -> {
            velocity = Vector(0.0, 1.8, 0.0)
            playSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH)
        }
        Material.MAGENTA_GLAZED_TERRACOTTA -> {
            velocity = location.clone().apply { pitch = 0F }.direction.apply { y = 0.9 }.multiply(1.7)
            playSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH)
        }
        else -> {}
    }
    if (blockBelow.y < 90) {
        teleport(world.spawnLocation.apply {
            yaw = location.yaw
            pitch = location.pitch
        })
        damage(7.0)
    }
}

fun checkFallDamage(blockBelow: Block) = when (blockBelow.type) {
    Material.WHITE_GLAZED_TERRACOTTA -> true
    else -> false
}