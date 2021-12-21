package de.lookonthebrightsi.arena

import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.requiresPermission
import net.axay.kspigot.commands.runs

fun commands() {
    // Toggle building
    command("build") {
        requiresPermission("arena.build")
        runs {
            player.combat = !player.combat
        }
    }
}