package de.lookonthebrightsi.arena

import org.bukkit.Material

data class Settings(
    val fallDamage: Setting = Setting("Fall Damage", Material.CHAINMAIL_BOOTS, true),
    val explosionDamage: Setting = Setting("Explosion Damage", Material.TNT, true),
) {
    fun asList(): List<Setting> {
        val list = arrayListOf<Setting>()
        javaClass.declaredFields.forEach {
            list += it.get(this) as Setting
        }
        return list
    }
}

var settings = Settings() //TODO save to json file

data class Setting(
    val name: String,
    val icon: Material,
    var value: Any,
) {
    fun boolean() = value as Boolean
}