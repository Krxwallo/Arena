group = "de.lookonthebrightsi"
version = "0.0.1"
val kspigot = "1.18.0"
val kutils = "0.0.5"

plugins {
    kotlin("jvm") version "1.6.10"
    id("io.papermc.paperweight.userdev") version "1.3.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

bukkit {
    main = "$group.arena.InternalMainClass"
    website = "https://github.com/HGLabor/training-v2"
    version = project.version.toString()
    apiVersion = "1.18"
    libraries = listOf(
        "net.axay:kspigot:$kspigot",
        "de.hglabor.utils:kutils:$kutils"
    )
}

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")
    implementation("net.axay:kspigot:$kspigot")
    implementation("de.hglabor.utils:kutils:$kutils")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
}

tasks {
    build {
        dependsOn(reobfJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}