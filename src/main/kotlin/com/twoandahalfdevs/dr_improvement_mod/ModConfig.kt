package com.twoandahalfdevs.dr_improvement_mod

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.FileReader
import java.io.FileWriter

data class ConfigData(
  var colorBlind: Boolean = false,
  var abilityNametags: Boolean = true,
  var usefulInfo: Boolean = true,
  var shadowmeldPoints: Int = 0,
  var justMyNaturePoints: Int = 0,
)

object ModConfig {
  private val GSON = GsonBuilder().setPrettyPrinting().create()
  private val CONFIG_FILE = FabricLoader.getInstance().configDir.resolve("dr_improvement_mod.json").toFile()

  var settings: ConfigData = ConfigData()
    private set

  fun save() {
    try {
      FileWriter(CONFIG_FILE).use { writer ->
        GSON.toJson(settings, writer)
      }
    } catch (ignored: Exception) {
    }
  }

  fun load() {
    if (CONFIG_FILE.exists()) {
      try {
        FileReader(CONFIG_FILE).use { reader ->
          settings = GSON.fromJson(reader, ConfigData::class.java)
        }
      } catch (ignored: Exception) {
        // Create default config
        settings = ConfigData()
        save()
      }
    } else {
      // If no file exists, create one with default values
      settings = ConfigData()
      save()
    }
  }
}