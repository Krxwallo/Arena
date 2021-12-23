package de.lookonthebrightsi.arena

import de.hglabor.utils.kutils.stack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.axay.kspigot.chat.KColors
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
@Serializable
class Equip(val armorItems: List<Item>, val weapons: List<Item>, var extras: Map<Material, Int>, val shield: Boolean = true) {
    fun extrasToItemStacks(): List<ItemStack> {
        val stacks = arrayListOf<ItemStack>()
        extras.forEach { (type, amount) ->
            stacks += type.stack(amount)
        }
        return stacks
    }
    fun setExtras(value: List<ItemStack?>) {
        val mExtras = hashMapOf<Material, Int>()
        value.forEach {
            if (it != null) mExtras += it.type to it.amount
        }
        extras = mExtras
    }

    fun giveTo(player: Player) {
        val armorContents = arrayListOf<ItemStack>()
        armorItems.forEach { armorContents.add(it.getItemStack()) }
        player.inventory.setArmorContents(armorContents.toTypedArray())

        weapons.forEach {
            if (it.slot != -1) player.inventory.setItem(it.slot, it.getItemStack())
            else player.give(it.getItemStack())
        }

        player.give(*extrasToItemStacks().toTypedArray())

        if (shield) player.inventory.setItemInOffHand(Material.SHIELD.stack())
    }
}

/** Default iron no sharp equip */
val DEFAULT_EQUIP = Equip(
    // Armor
    listOf(Boots(level = 2), Leggings(level = 2), Chestplate(level = 2), Helmet(level = 2)),
    // Weapons
    listOf(Sword(level = 2), Axe(level = 2), Bow()),
    // Extras
    mapOf(Material.ENDER_PEARL to 1, Material.ARROW to 1, Material.COOKED_BEEF to 64))

var equip = DEFAULT_EQUIP

/** Represents an equip item, like a sword or an axe */
@Serializable
sealed class Item(
    val minLevel: Int = 2,
    private val maxLevel: Int = 4,
    val maxModifier: Int = 5
) {
    abstract val itemType: String
    abstract var level: Int // -1 for N/A
    abstract var modifier: Int // -1 for N/A
    /** If this is `-1`, the item is just given to the best slot in the inventory */
    abstract val slot: Int
    private val material get() = if (itemType.equals("bow", true)) Material.BOW else Material.valueOf(("${levels[level]}_${javaClass.simpleName}").uppercase())
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
@Serializable
@SerialName("sword")
class Sword(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 0): Item(minLevel = 0) {
    override val itemType = "Sword"
}
@Serializable
@SerialName("axe")
class Axe(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 1): Item(minLevel = 0) {
    override val itemType = "Axe"
}
@Serializable
@SerialName("bow")
class Bow(override var modifier: Int = 0, override val slot: Int = 2): Item() {
    override val itemType = "Bow"
    override var level = -1 // There are no bow types
}

// Armor needs to be level iron or up and max. protection IV
@Serializable
@SerialName("helmet")
class Helmet(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 103): Item(maxModifier = 4) {
    override val itemType = "Helmet"
}
@Serializable
@SerialName("chestplate")
class Chestplate(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 102): Item(maxModifier = 4) {
    override val itemType = "Chestplate"
}
@Serializable
@SerialName("leggings")
class Leggings(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 101): Item(maxModifier = 4) {
    override val itemType = "Leggings"
}
@Serializable
@SerialName("boots")
class Boots(override var level: Int = 2, override var modifier: Int = 0, override val slot: Int = 100): Item(maxModifier = 4) {
    override val itemType = "Boots"
}
