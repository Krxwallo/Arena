package de.lookonthebrightsi.arena


import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.main.KSpigot
import org.bukkit.ChatColor
import java.io.File

val Manager by lazy { InternalMainClass.INSTANCE }

val PREFIX: String = "${KColors.DARKGRAY}[${ChatColor.AQUA}${KColors.BOLD}Arena${ChatColor.DARK_GRAY}]${ChatColor.WHITE}"
val DEBUG: String = "${KColors.DARKGRAY}[${ChatColor.AQUA}${KColors.BOLD}Debug${ChatColor.DARK_GRAY}]${ChatColor.WHITE}"

class InternalMainClass : KSpigot() {
    private val configFile by lazy {
        dataFolder.mkdir()
        File(dataFolder.path + "/config.json")
    }

    companion object {
        lateinit var INSTANCE: InternalMainClass; private set
    }

    override fun load() {
        INSTANCE = this
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        prettyPrintIndent = "  "
    }

    override fun startup() {
        commands()
        events()
        // Deserialize equip
        if (!configFile.exists()) logger.warning("No existing config file")
        else equip = json.decodeFromString(configFile.readText())
        // For reloads
        combatPlayers { reEquip() }
    }

    override fun shutdown() {
        // Serialize equip TODO for each team
        configFile.writeText(json.encodeToString(equip))
    }
}
