package de.lookonthebrightsi.arena

import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.reflectMethod
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.SingleListener
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.bukkit.actionBar
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPlaceEvent
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
        if (it.clickedBlock?.type?.isInteractable == true) {
            it.cancel()
            it.player.actionBar("${KColors.RED}You can't interact here")
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
        if (it.player.combat) it.player.equip(DEFAULT_EQUIP) // TODO equip corresponding to team
    }

    listen<LeavesDecayEvent> {
        it.cancel()
    }

    listen<PlayerGameModeChangeEvent> {
        //if (it.newGameMode == GameMode.SURVIVAL) it.player.combat = false
    }

    combatListen<PlayerMoveEvent> {
        val oldBlock = it.from.block.getRelative(BlockFace.DOWN)
        val newBlock = it.to.block.getRelative(BlockFace.DOWN)
        if (oldBlock != newBlock) it.player.checkMechanics(newBlock)
    }

    listen<EntityDamageEvent> {
        if (it.cause == EntityDamageEvent.DamageCause.FALL
            && it.entity is Player && (it.entity as Player).combat
            && checkFallDamage(it.entity.location.block.getRelative(BlockFace.DOWN))) it.cancel()
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