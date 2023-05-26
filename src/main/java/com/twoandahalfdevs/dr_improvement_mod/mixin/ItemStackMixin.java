package com.twoandahalfdevs.dr_improvement_mod.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
  private final Pattern originRegex = Pattern.compile("(.*) \\((.*)/.*\\)");

  @Shadow
  private @Nullable NbtCompound nbt;

  @Shadow
  public abstract Text getName();

  @Inject(method = "getTooltip", at = @At("RETURN"))
  private void getTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
    // Adds item origin as a tooltip
    List<Text> tooltipList = cir.getReturnValue();
    List<Text> additionalList = new ArrayList<>();

    if (this.nbt != null && this.nbt.contains("origin")) {
      String origin = this.nbt.getString("origin");
      Matcher matcher = originRegex.matcher(origin);
      if (matcher.find()) {
        if (matcher.groupCount() == 2) {
          String originType = matcher.group(1);
          String playerStr = matcher.group(2);

          additionalList.add(Text.of(Formatting.GRAY + "Origin: " + originType + " - " + Formatting.ITALIC + playerStr));
        }
      }
    }

    if (!additionalList.isEmpty()) {
      tooltipList.add(Text.of(""));
      tooltipList.addAll(additionalList);
    }
  }
}
