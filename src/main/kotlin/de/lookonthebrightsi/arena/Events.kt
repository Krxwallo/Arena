package de.lookonthebrightsi.arena

import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.isRightClick
import de.hglabor.utils.kutils.reflectMethod
import de.hglabor.utils.kutils.stack
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.SingleListener
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.bukkit.actionBar
import net.axay.kspigot.extensions.bukkit.give
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.block.BlockFace
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.*

fun events() {
    // Cancel stuff when player is in combat
    combatListen<BlockPlaceEvent> {
        it.cancel()
        it.player.actionBar("${KColors.RED}You can't place blocks here")
    }
    combatListen<BlockBreakEvent> {
        it.cancel()
        it.player.actionBar("${KColors.RED}You can't break blocks here")
    }
    combatListen<EntityPlaceEvent> {
        it.cancel()
        it.player?.actionBar("${KColors.RED}You can't place entities here")
    }
    combatListen<PlayerInteractEvent> {
        if (it.isRightClick && it.clickedBlock?.type?.isInteractable == true) {
            it.cancel()
            it.player.actionBar("${KColors.RED}You can't interact here")
        }
        else if (it.isRightClick) equip.specialItems.forEach { item ->
            if (item.item == it.item?.type) {
                it.actionForItem(item.item)
                taskRunLater((20*item.cooldown).toLong()) {
                    it.player.inventory.remove(item.item) // TODO correct fix
                    it.player.give(item.item.stack())
                }
            }
        }
    }
    combatListen<PlayerItemConsumeEvent> { event ->
        equip.specialItems.forEach {
            if (it.enabled && it.item == event.item.type) event.cancel()
        }
    }
    combatListen<PlayerItemDamageEvent> {
        it.cancel()
    }
    combatListen<PlayerDropItemEvent> {
        it.cancel()
        it.player.actionBar("${KColors.RED}You can't drop items here")
    }

    listen<PlayerJoinEvent> {
        // Equip player when in combat
        if (it.player.combat) {
            it.player.reEquip() // TODO equip corresponding to team
        }
    }

    listen<LeavesDecayEvent> {
        it.cancel()
    }

    listen<PlayerGameModeChangeEvent> {
        //if (it.newGameMode == GameMode.SURVIVAL) it.player.combat = false
        if (!it.player.isOnline) return@listen
        /*if (it.newGameMode == GameMode.SURVIVAL && !it.player.combat) {
            it.cancel()
            it.player.combat = true
        }
        else if (it.player.gameMode == GameMode.SURVIVAL && it.player.combat) {
            it.cancel()
            it.player.combat = false
        }*/
    }

    combatListen<PlayerMoveEvent> {
        val oldBlock = it.from.block.getRelative(BlockFace.DOWN)
        val newBlock = it.to.block.getRelative(BlockFace.DOWN)
        if (oldBlock != newBlock) it.player.checkMechanics(newBlock)
    }

    combatListen<PlayerTeleportEvent> {
        val blockBelow = it.player.location.block.getRelative(BlockFace.DOWN)
        it.player.checkMechanics(blockBelow)
    }

    listen<EntityShootBowEvent> {
        it.setConsumeItem(false) // TODO setting
    }

    listen<EntityDamageEvent> {
        if (it.cause == EntityDamageEvent.DamageCause.FALL
            && it.entity is Player && (it.entity as Player).combat
            && checkFallDamage(it.entity.location.block.getRelative(BlockFace.DOWN))) it.cancel()
    }

    // NED
    listen<EntityDamageByEntityEvent> {
        if (it.entity is Player && it.damager is EnderPearl) it.cancel()
    }

    listen<PlayerRespawnEvent> {
        it.respawnLocation = it.player.world.spawnLocation
    }
}

/** Listen for player events, but only run the block when the player is in combat */
inline fun <reified T : Event> combatListen(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    register: Boolean = true,
    crossinline onEvent: (event: T) -> Unit,
): SingleListener<T> = listen {
    val player = it.reflectMethod<Player>("getPlayer")
    if (player != null) { if (player.combat) onEvent(it) }
    else broadcast("${KColors.RED}DU HAST SCHEISSE GEBAUT ${it.eventName} hat kein .getPlayer()")
}