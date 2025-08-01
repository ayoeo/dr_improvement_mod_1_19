package com.twoandahalfdevs.dr_improvement_mod.mixin;

import com.twoandahalfdevs.dr_improvement_mod.DrImprovementModKt;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.BossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public class BossBarMixin {
  @ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 1), index = 3)
  private int modifyHeight(int height) {
    // If there are more than 2 boss bars I'm gonna freak out
    return height + 14;
  }

  @Inject(method = "renderBossBar(Lnet/minecraft/client/util/math/MatrixStack;IILnet/minecraft/entity/boss/BossBar;II)V", at = @At("HEAD"), cancellable = true)
  private void renderBossBarHead(MatrixStack matrices, int x, int y, BossBar bar, int width, int vOffset, CallbackInfo ci) {
    // Don't render the bar for player hp, just the text
    var plainText = bar.getName().getString();
    if (plainText.contains("Lv ") && plainText.contains(" - HP ")) {
      try {
        var split = plainText.split("-");
        DrImprovementModKt.setClas(split[0].trim());
      } catch (Exception ignored) {
      }

      ci.cancel();
    }

  }
}
