package de.lookonthebrightsi.arena

import de.hglabor.utils.kutils.stack
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.bukkit.give
import net.axay.kspigot.items.addLore
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val levels = listOf(
    "Wooden",
    "Stone",
    "Iron",
    "Diamond",
    "Netherite",
)

/**
 * Levels:
 * 0 Wood // Invalid on armor
 * 1 Stone // Invalid on armor
 * 2 Iron
 * 3 Diamond
 * 4 Netherite
 *
 * Modifiers:
 * 0 No Sharpness / No Protection
 * 1 Sharpness I   / Protection I
 * 2 Sharpness II  / Protection II
 * 3 Sharpness III / Protection III
 * ...
 */
data class Equip(val armorItems: List<Item>, val weapons: List<Item>, val extras: List<ItemStack>) {
    fun giveTo(player: Player) {
        val armorContents = arrayListOf<ItemStack>()
        armorItems.forEach { armorContents.add(it.getItemStack()) }
        player.inventory.setArmorContents(armorContents.toTypedArray())

        weapons.forEach {
            if (it.slot != -1) player.inventory.setItem(it.slot, it.getItemStack())
            else player.give(it.getItemStack())
        }

        extras.forEach {
            if (it.type == Material.SHIELD) player.inventory.setItemInOffHand(it)
            else player.give(it)
        }
    }
}

/** Default iron no sharp equip */
val DEFAULT_EQUIP = Equip(
    // Armor
    listOf(Boots(level = "Diamond"), Leggings(level = "Diamond"), Chestplate(level = "Diamond", modifier = 2), Helmet(level = "Diamond")),
    // Weapons
    listOf(Sword(level = "Netherite"), Axe(level = "Diamond", modifier = 1)),
    // Extras
    listOf(Material.SHIELD.stack(), itemStack(Material.BOW) {
        addEnchantment(Enchantment.ARROW_INFINITE, 1)
    }, Material.ARROW.stack(), Material.COOKED_BEEF.stack(64)))

/** Represents an equip item, like a sword or an axe */
abstract class Item {
    abstract val level: String
    abstract val modifier: Int
    /** If this is `-1`, the item is just given to the best slot in the inventory */
    open val slot: Int = -1
    private val material get() = Material.valueOf(("${level}_${javaClass.simpleName}").uppercase())
    private val itemType get() = javaClass.simpleName
    /** Return the `ItemStack` representation of this item */
    fun getItemStack(): ItemStack {
        // Material = LEVELSTRING_CLASSNAME
        // E.g. IRON_SWORD

        return itemStack(material) {
            // Description
            meta {
                name = "${KColors.YELLOW}${KColors.BOLD}$itemType"
                addLore {
                    lorelist += "${KColors.AQUA}Level: $level"
                    if (modifier > 0) lorelist += "${KColors.GREEN}Modifier: $modifier"
                }
            }
            // Enchantments
            if (modifier > 0) when (itemType.lowercase()) {
                "sword", "axe" -> addEnchantment(Enchantment.DAMAGE_ALL, modifier)
                "helmet", "chestplate", "leggings", "boots" -> addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, modifier)
            }
        }
    }
}

/** Get the `Item` representation of this `ItemStack` */
fun ItemStack.parseAsItem() {
    // TODO idk if this is needed
}

// Default Level = Iron, Default Modifier = 0
data class Sword(override val level: String = "Iron", override val modifier: Int = 0, override val slot: Int = 0): Item()
data class Axe(override val level: String = "Iron", override val modifier: Int = 0, override val slot: Int = 1): Item()

data class Helmet(override val level: String = "Iron", override val modifier: Int = 0, override val slot: Int = 103): Item()
data class Chestplate(override val level: String = "Iron", override val modifier: Int = 0, override val slot: Int = 102): Item()
data class Leggings(override val level: String = "Iron", override val modifier: Int = 0, override val slot: Int = 101): Item()
data class Boots(override val level: String = "Iron", override val modifier: Int = 0, override val slot: Int = 100): Item()
