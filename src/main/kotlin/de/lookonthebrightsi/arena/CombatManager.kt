package de.lookonthebrightsi.arena

import de.hglabor.utils.kutils.*
import net.axay.kspigot.chat.KColors
import org.bukkit.entity.Player

var Player.combat: Boolean
    get() = isSurvival()
    set(value) {
        if (value == combat) return
        if (value) {
            closeInventory(); clearHealFeedSaturate()
            survival()
            // TODO give equip of team
            equip(DEFAULT_EQUIP)
            sendMessage("$PREFIX ${KColors.GREEN}You are now in combat.")
        }
        else {
            closeInventory(); clearHealFeedSaturate()
            creative()
            sendMessage("$PREFIX ${KColors.YELLOW}You are now in building mode.")
        }
    }

fun Player.equip(equip: Equip) {
    equip.giveTo(this)
}