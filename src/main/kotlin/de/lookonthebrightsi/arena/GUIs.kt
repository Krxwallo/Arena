package de.lookonthebrightsi.arena

import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.namedItem
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.SingleListener
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.elements.GUIRectSpaceCompound
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.axay.kspigot.items.setLore
import net.axay.kspigot.runnables.taskRun
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.InventoryView

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

fun Player.openMainGui() = openGUI(kSpigotGUI(GUIType.ONE_BY_FIVE) {
    title = "${KColors.DARKGREEN}Arena"
    page(1) {
        button(Slots.RowOneSlotTwo, namedItem(Material.GOLDEN_SWORD, "${KColors.YELLOW}Teams")) {
            openTeamsGui()
        }
        button(Slots.RowOneSlotFour, namedItem(Material.COMPARATOR, "${KColors.GRAY}Settings")) {
            //openSettingsGui() // TODO
            sendMessage("N/I")
        }
    }
})

fun Player.openTeamsGui(): InventoryView? = openGUI(kSpigotGUI(GUIType.FIVE_BY_NINE) {
    title = "${KColors.BLUE}Team 1 - Equip"

    page(1) {
        placeholder(Slots.All, namedItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, ""))

        lateinit var levelCompound: GUIRectSpaceCompound<*, Item>
        lateinit var itemCompound: GUIRectSpaceCompound<*, Item>
        lateinit var modifierCompound: GUIRectSpaceCompound<*, Item>

        fun updateCompounds() {
            // TODO real equip
            val items = equip.weapons + equip.armorItems
            levelCompound.setContent(items)
            itemCompound.setContent(items)
            modifierCompound.setContent(items)
        }

        levelCompound = createRectCompound(
            Slots.RowFourSlotTwo,
            Slots.RowFourSlotEight,
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
            Slots.RowThreeSlotTwo,
            Slots.RowThreeSlotEight,
            iconGenerator = { it.getItemStack() },

            onClick = { clickEvent, _ ->
                clickEvent.bukkitEvent.cancel() // TODO Enable/Disable
            }
        )

        modifierCompound = createRectCompound(
            Slots.RowTwoSlotTwo,
            Slots.RowTwoSlotEight,
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

        button(Slots.RowOneSlotOne, namedItem(Material.ENDER_PEARL, "${KColors.GREEN}Special Items")) {

        }

        button(Slots.RowOneSlotNine, namedItem(Material.BUNDLE, "${KColors.BROWN}Extras")) {
            val inv = Bukkit.createInventory(null, 27, "${KColors.BROWN}Extras")
            inv.addItem(*equip.extrasToItemStacks().toTypedArray())
            openInventory(inv)
            @Suppress("JoinDeclarationAndAssignment")
            lateinit var listener: SingleListener<InventoryCloseEvent>
            listener = listen {
                if (it.player == this@openTeamsGui && it.inventory == inv) {
                    listener.unregister()
                    equip.setExtras(it.inventory.contents?.toList() ?: return@listen)
                    taskRunLater(1L ) { openTeamsGui() }
                }
            }
        }

        updateCompounds()
    }

    onClose {
        // TODO no reequip when opening b
        sendMessage("$PREFIX ${KColors.GREEN}Saved equipment")
        combatPlayers { reEquip() }
    }
})