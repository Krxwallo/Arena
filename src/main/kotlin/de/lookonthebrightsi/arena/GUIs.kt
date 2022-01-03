package de.lookonthebrightsi.arena

import de.hglabor.utils.kutils.cancel
import de.hglabor.utils.kutils.inv
import de.hglabor.utils.kutils.namedItem
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
import net.axay.kspigot.runnables.taskRunLater
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
            openSpecialItemsGui()
        }

        button(Slots.RowOneSlotNine, namedItem(Material.BUNDLE, "${KColors.BROWN}Extras")) {
            inv(27, "${KColors.BROWN}Extras", equip.extrasToItemStacks()) {
                equip.setExtras(inventory.contents?.toList() ?: return@inv)
                taskRunLater(1L ) { openTeamsGui() }
            }
        }

        updateCompounds()
    }

    onClose {
        if (it.bukkitEvent.reason == InventoryCloseEvent.Reason.PLAYER) {
            sendMessage("$PREFIX ${KColors.GREEN}Saved equipment")
            combatPlayers { reEquip() }
        }
    }
})

fun Player.openSpecialItemsGui(): InventoryView? = openGUI(kSpigotGUI(GUIType.FOUR_BY_NINE) {
    title = "${KColors.DARKGREEN}Special Items"

    page(1) {
        placeholder(Slots.All, namedItem(Material.LIME_STAINED_GLASS_PANE, ""))

        lateinit var itemsCompound: GUIRectSpaceCompound<*, CooldownItem>
        lateinit var onOffCompound: GUIRectSpaceCompound<*, CooldownItem>

        fun updateCompounds() {
            // TODO real equip
            itemsCompound.setContent(equip.specialItems)
            onOffCompound.setContent(equip.specialItems)
        }

        itemsCompound = createRectCompound(
            Slots.RowThreeSlotTwo,
            Slots.RowThreeSlotEight,
            iconGenerator = {
                it.item.stack().apply {
                    meta {
                        name = if (!it.enabled) "${KColors.RED}${KColors.STRIKETHROUGH}${it.item.name}" else "${KColors.GREEN}${it.item.name}"
                        setLore {
                            lorelist += "${KColors.LIME}Cooldown: ${it.cooldown}"
                            if (it.cooldown < 180) lorelist += "${KColors.LIGHTGRAY}Left click to increase cooldown"
                            if (it.cooldown <= 170) lorelist += "${KColors.LIGHTGRAY}Shift Left click to increase cooldown by 10"
                            if (it.cooldown > 0) lorelist += "${KColors.LIGHTGRAY}Right click to decrease cooldown"
                            if (it.cooldown >= 10) lorelist += "${KColors.LIGHTGRAY}Shift Right click to decrease cooldown by 10"
                        }
                    }
                }
            },
            onClick = { clickEvent, element ->
                clickEvent.bukkitEvent.cancel()
                if (clickEvent.bukkitEvent.isRightClick) {
                    if (clickEvent.bukkitEvent.isShiftClick && element.cooldown >= 10) element.cooldown-=10
                    else if (!clickEvent.bukkitEvent.isShiftClick && element.cooldown > 0) element.cooldown--
                }
                else if (clickEvent.bukkitEvent.isLeftClick) {
                    if (clickEvent.bukkitEvent.isShiftClick && element.cooldown <= 170) element.cooldown+= 10
                    else if (!clickEvent.bukkitEvent.isShiftClick && element.cooldown < 180) element.cooldown++
                }
                updateCompounds()
            }
        )

        onOffCompound = createRectCompound(
            Slots.RowTwoSlotTwo,
            Slots.RowTwoSlotEight,
            iconGenerator = {
                if (it.enabled) itemStack(Material.GREEN_WOOL) {
                    meta {
                        name = "${KColors.GREEN}Enabled"
                        setLore {
                            lorelist += "${KColors.LIGHTGRAY}Click to ${KColors.RED}disable"
                        }
                    }
                }
                else itemStack(Material.RED_WOOL) {
                    meta {
                        name = "${KColors.RED}Disabled"
                        setLore {
                            lorelist += "${KColors.LIGHTGRAY}Click to ${KColors.GREEN}enable"
                        }
                    }
                }
            },
            onClick = { clickEvent, element ->
                clickEvent.bukkitEvent.cancel()
                element.enabled = !element.enabled
                updateCompounds()
            }
        )

        updateCompounds()

        onClose {
            taskRunLater(1L) { openTeamsGui() }
        }
    }
})