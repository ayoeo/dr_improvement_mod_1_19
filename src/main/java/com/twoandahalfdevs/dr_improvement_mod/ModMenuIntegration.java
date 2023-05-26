package com.twoandahalfdevs.dr_improvement_mod;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return parent -> {
      ConfigBuilder builder = ConfigBuilder.create()
        .setParentScreen(parent)
        .setTitle(Text.of("DR Improvement Mod Config"));
      ConfigCategory general = builder.getOrCreateCategory(Text.of("general"));

      ConfigEntryBuilder entryBuilder = builder.entryBuilder();

      DrImprovementConfig defaultConfig = DrImprovementConfig.DEFAULT;

      BooleanListEntry disableWTapSprint = entryBuilder
        .startBooleanToggle(Text.of("Disable W-Tap Sprint"), defaultConfig.shouldDisableWTapSprint())
        .setDefaultValue(defaultConfig.shouldDisableWTapSprint())
        .build();

      general.addEntry(disableWTapSprint);

      // TODO - make this thing actually work ahhahahah

      return builder.build();
    };
  }
}