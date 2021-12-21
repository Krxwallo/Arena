package de.lookonthebrightsi.arena

import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.stack
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.elements.GUIRectSpaceCompound
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun Player.openSettingsGui() = openGUI(kSpigotGUI(GUIType.FOUR_BY_NINE) {
    title = "${KColors.AQUA}Items Editor"

    page(1) {
        placeholder(Slots.All, Material.LIGHT_BLUE_STAINED_GLASS_PANE.stack())

        lateinit var compound: GUIRectSpaceCompound<*, ItemStack>
        compound = createRectCompound(
            Slots.RowTwoSlotTwo,
            Slots.RowTwoSlotEight,
            iconGenerator = { it },

            onClick = { clickEvent, element ->
                clickEvent.bukkitEvent.cancel()
            }
        )

        //compound.addContent(listOf(DAMAGER, MLG))
    }
})