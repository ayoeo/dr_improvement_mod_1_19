package com.twoandahalfdevs.dr_improvement_mod

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class ModMenuIntegration : ModMenuApi {
  override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
    return ConfigScreenFactory(ModConfigScreen::create)
  }
}

object ModConfigScreen {
  fun create(parent: Screen): Screen {
    val builder = ConfigBuilder.create()
      .setParentScreen(parent)
      .setTitle(Text.literal("DR Improvement Mod Settings"))

    builder.setSavingRunnable(ModConfig::save)

    val general = builder.getOrCreateCategory(Text.literal("General"))
    val entryBuilder = builder.entryBuilder()

    general.addEntry(
      entryBuilder.startBooleanToggle(Text.literal("Colorblind Glow"), ModConfig.settings.colorBlind)
        .setDefaultValue(false)
        .setTooltip(Text.literal("for riph"))
        .setSaveConsumer { newValue -> ModConfig.settings.colorBlind = newValue }
        .build()
    )

    general.addEntry(
      entryBuilder.startBooleanToggle(Text.literal("Useful Info"), ModConfig.settings.usefulInfo)
        .setDefaultValue(false)
        .setTooltip(Text.literal("Combat timer, pot cooldown, ability cooldown yay."))
        .setSaveConsumer { newValue -> ModConfig.settings.usefulInfo = newValue }
        .build()
    )

    general.addEntry(
      entryBuilder.startBooleanToggle(Text.literal("Ability Nametags"), ModConfig.settings.abilityNametags)
        .setDefaultValue(false)
        .setTooltip(Text.literal("Ability durations / cooldowns in the nametags."))
        .setSaveConsumer { newValue -> ModConfig.settings.abilityNametags = newValue }
        .build()
    )

    general.addEntry(
      entryBuilder.startIntSlider(Text.literal("Shadowmeld Points"), ModConfig.settings.shadowmeldPoints, 0, 10)
        .setDefaultValue(5)
        .setTooltip(Text.literal("Sets Rogue 'Shadowmeld' node points."))
        .setSaveConsumer { newValue -> ModConfig.settings.shadowmeldPoints = newValue }
        .build()
    )

    general.addEntry(
      entryBuilder.startIntSlider(Text.literal("Just My Nature Points"), ModConfig.settings.justMyNaturePoints, 0, 5)
        .setDefaultValue(2)
        .setTooltip(Text.literal("Sets Rogue 'It's Just My Nature' node points."))
        .setSaveConsumer { newValue -> ModConfig.settings.justMyNaturePoints = newValue }
        .build()
    )

    return builder.build()
  }
}