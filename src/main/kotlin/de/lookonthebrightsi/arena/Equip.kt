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

val levelMaterials = listOf(
    Material.OAK_PLANKS,
    Material.COBBLESTONE,
    Material.IRON_INGOT,
    Material.DIAMOND,
    Material.NETHERITE_INGOT,
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
data class Equip(val armorItems: List<Item>, val weapons: List<Item>, val extras: List<ItemStack>, val shield: Boolean = true) {
    fun giveTo(player: Player) {
        val armorContents = arrayListOf<ItemStack>()
        armorItems.forEach { armorContents.add(it.getItemStack()) }
        player.inventory.setArmorContents(armorContents.toTypedArray())

        weapons.forEach {
            if (it.slot != -1) player.inventory.setItem(it.slot, it.getItemStack())
            else player.give(it.getItemStack())
        }

        player.give(*extras.toTypedArray())

        if (shield) player.inventory.setItemInOffHand(Material.SHIELD.stack())
    }
}

/** Default iron no sharp equip */
val TEST_EQUIP = Equip(
    // Armor
    listOf(Boots(level = 2), Leggings(level = 2), Chestplate(level = 2), Helmet(level = 2)),
    // Weapons
    listOf(Sword(level = 2), Axe(level = 2), Bow()),
    // Extras
    listOf(Material.ARROW.stack(), Material.COOKED_BEEF.stack(64)))

/** Represents an equip item, like a sword or an axe */
abstract class Item {
    abstract var level: Int // -1 for N/A
    abstract var modifier: Int // -1 for N/A
    open val maxModifier: Int =  5
    open val minLevel: Int = 2
    open val maxLevel: Int = 4
    /** If this is `-1`, the item is just given to the best slot in the inventory */
    open val slot: Int = -1
    private val material get() = if (itemType.equals("bow", true)) Material.BOW else Material.valueOf(("${levels[level]}_${javaClass.simpleName}").uppercase())
    val itemType: String get() = javaClass.simpleName
    /** Return the `ItemStack` representation of this item */
    fun getItemStack(): ItemStack {
        // Material = LEVELSTRING_CLASSNAME
        // E.g. IRON_SWORD

        return itemStack(material) {
            // Description
            meta {
                name = "${KColors.YELLOW}${KColors.BOLD}$itemType"
                addLore {
                    if (level != -1) lorelist += "${KColors.AQUA}Level: ${levels[level]}"
                    if (modifier > 0) lorelist += "${KColors.GREEN}Modifier: $modifier"
                }
            }
            // Enchantments
            if (modifier > 0) when (itemType.lowercase()) {
                "sword", "axe" -> addEnchantment(Enchantment.DAMAGE_ALL, modifier)
                "helmet", "chestplate", "leggings", "boots" -> addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, modifier)
                "bow" -> addEnchantment(Enchantment.ARROW_DAMAGE, modifier)
            }
        }
    }

    fun levelIncreasable() = level < maxLevel
    fun levelDecreasable() = level > minLevel
    fun modifierIncreasable() = modifier < maxModifier
    fun modifierDecreasable() = modifier > 0
}

/** Get the `Item` representation of this `ItemStack` */
fun ItemStack.parseAsItem() {
    // TODO idk if this is needed
}

// Default Level = Iron, Default Modifier = 0
data class Sword(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 0): Item() {
    override val minLevel = 0
}
data class Axe(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 1): Item() {
    override val minLevel = 0
}
data class Bow(override var modifier: Int = 0, override val slot: Int = 2): Item() {
    override var level: Int = -1 // There are no bow types
}

// Armor needs to be level iron or up and max. protection IV
data class Helmet(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 103): Item() {
    override val minLevel = 2
    override val maxModifier = 4
}
data class Chestplate(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 102): Item() {
    override val minLevel = 2
    override val maxModifier = 4
}
data class Leggings(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 101): Item() {
    override val minLevel = 2
    override val maxModifier = 4
}
data class Boots(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 100): Item() {
    override val minLevel = 2
    override val maxModifier = 4
}
