package com.twoandahalfdevs.dr_improvement_mod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(Team.class)
public abstract class TeamMixin {
  @Shadow
  public abstract Formatting getColor();

  @Shadow
  private Text prefix;

  @Shadow
  private Text suffix;

  /**
   * @author mhm
   * @reason sure
   */
  @Overwrite
  public MutableText decorateName(Text name) {
    var player = MinecraftClient.getInstance().player;
    Text selfPrefix = null;
    if (player != null && player.getScoreboardTeam() instanceof Team) {
      selfPrefix = ((Team) player.getScoreboardTeam()).getPrefix();
    }

    Text colChange = this.prefix;
    Text sufx = this.suffix;
    if (this.prefix.getString().startsWith("[") && Objects.equals(this.prefix.getString(), selfPrefix.getString())) {
      colChange = this.prefix.copy();
      var siblings = colChange.getSiblings();
      if (siblings != null && !siblings.isEmpty()) {
        var c = siblings.get(0).copy();

        // Don't change party colors
        if (!Objects.equals(c.getStyle().getColor().getName(), "dark_green")) {
          var greenTag = c.setStyle(Style.EMPTY.withColor(Formatting.GREEN));
          colChange.getSiblings().set(0, greenTag);
          sufx = this.suffix.copy().setStyle(Style.EMPTY.withColor(Formatting.GREEN));
        }
      }
    }

    MutableText mutableText = Text.empty().append(colChange).append(name).append(sufx);
    Formatting formatting = this.getColor();
    if (formatting != Formatting.RESET) {
      mutableText.formatted(formatting);
    }

    return mutableText;
  }
}
