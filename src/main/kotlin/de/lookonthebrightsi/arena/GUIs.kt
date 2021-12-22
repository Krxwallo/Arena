package de.lookonthebrightsi.arena

import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.stack
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.elements.GUIRectSpaceCompound
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.axay.kspigot.items.setLore
import org.bukkit.Material
import org.bukkit.entity.Player

val LEVEL_NOT_AVAILABLE = itemStack(Material.BARRIER) {
    meta {
        name = "${KColors.RED}Not available"
        setLore { lorelist += "${KColors.LIGHTGRAY}This item has no levels" }
    }
}

val MODIFIER_NOT_AVAILABLE = itemStack(Material.BARRIER) {
    meta {
        name = "${KColors.RED}Not available"
        setLore { lorelist += "${KColors.LIGHTGRAY}This item has no modifier" }
    }
}

fun Player.openSettingsGui() = openGUI(kSpigotGUI(GUIType.SIX_BY_NINE) {
    title = "${KColors.BLUE}Team 1 - Equip"

    page(1) {
        placeholder(Slots.All, Material.LIGHT_BLUE_STAINED_GLASS_PANE.stack())

        lateinit var levelCompound: GUIRectSpaceCompound<*, Item>
        lateinit var itemCompound: GUIRectSpaceCompound<*, Item>
        lateinit var modifierCompound: GUIRectSpaceCompound<*, Item>

        fun updateCompounds() {
            // TODO real equip
            val items = TEST_EQUIP.weapons + TEST_EQUIP.armorItems
            levelCompound.setContent(items)
            itemCompound.setContent(items)
            modifierCompound.setContent(items)
        }

        levelCompound = createRectCompound(
            Slots.RowFiveSlotTwo,
            Slots.RowFiveSlotEight,
            iconGenerator = {
                if (it.level == -1) LEVEL_NOT_AVAILABLE
                else itemStack(levelMaterials[it.level]) {
                    meta {
                        name = "${KColors.AQUA}Level: ${levels[it.level]}"
                        setLore {
                            if (it.levelIncreasable()) lorelist += "${KColors.LIGHTGRAY}Left click to increase level"
                            if (it.levelDecreasable()) lorelist += "${KColors.LIGHTGRAY}Right click to decrease level"
                        }
                    }
                }
            },

            onClick = { clickEvent, item ->
                clickEvent.bukkitEvent.cancel()
                if (clickEvent.bukkitEvent.currentItem?.type == Material.BARRIER) return@createRectCompound
                if (clickEvent.bukkitEvent.isLeftClick && item.levelIncreasable()) item.level++
                else if (clickEvent.bukkitEvent.isRightClick && item.levelDecreasable()) item.level--
                updateCompounds()
            }
        )

        itemCompound = createRectCompound(
            Slots.RowFourSlotTwo,
            Slots.RowFourSlotEight,
            iconGenerator = { it.getItemStack() },

            onClick = { clickEvent, _ ->
                clickEvent.bukkitEvent.cancel() // TODO Enable/Disable
            }
        )

        modifierCompound = createRectCompound(
            Slots.RowThreeSlotTwo,
            Slots.RowThreeSlotEight,
            iconGenerator = {
                if (it.modifier == -1) MODIFIER_NOT_AVAILABLE
                else itemStack(if (it.modifier == 0) Material.BOOK else Material.ENCHANTED_BOOK) {
                    meta {
                        name = "${KColors.GREEN}Modifier: ${it.modifier}"
                        setLore {
                            if (it.modifierIncreasable()) lorelist += "${KColors.LIGHTGRAY}Left click to increase modifier"
                            if (it.modifierDecreasable()) lorelist += "${KColors.LIGHTGRAY}Right click to decrease modifier"
                        }
                    }
                }
            },

            onClick = { clickEvent, item ->
                clickEvent.bukkitEvent.cancel()
                if (clickEvent.bukkitEvent.isLeftClick && item.modifierIncreasable()) item.modifier++
                else if (clickEvent.bukkitEvent.isRightClick && item.modifierDecreasable()) item.modifier--
                updateCompounds()
            }
        )

        // Extra Items TODO compound
        freeSlot(Slots.RowOneSlotTwo)
        freeSlot(Slots.RowOneSlotThree)
        freeSlot(Slots.RowOneSlotFour)
        freeSlot(Slots.RowOneSlotFive)
        freeSlot(Slots.RowOneSlotSix)
        freeSlot(Slots.RowOneSlotSeven)
        freeSlot(Slots.RowOneSlotEight)

        updateCompounds()
    }

    onClose {
        sendMessage("$PREFIX ${KColors.GREEN}Saved equipment")
        combatPlayers { reEquip() }
    }
})