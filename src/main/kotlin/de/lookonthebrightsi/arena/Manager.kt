package de.lookonthebrightsi.arena

import net.axay.kspigot.chat.KColors
import net.axay.kspigot.main.KSpigot
import org.bukkit.ChatColor

val Manager by lazy { InternalMainClass.INSTANCE }

val PREFIX: String = "${KColors.DARKGRAY}[${ChatColor.AQUA}${KColors.BOLD}Arena${ChatColor.DARK_GRAY}]${ChatColor.WHITE}"
val DEBUG: String = "${KColors.DARKGRAY}[${ChatColor.AQUA}${KColors.BOLD}Debug${ChatColor.DARK_GRAY}]${ChatColor.WHITE}"

class InternalMainClass : KSpigot() {
    companion object {
        lateinit var INSTANCE: InternalMainClass; private set
    }

    override fun load() {
        INSTANCE = this
    }

    override fun startup() {
        commands()
        events()
        // For reloads
        combatPlayers { reEquip() }
    }

}


