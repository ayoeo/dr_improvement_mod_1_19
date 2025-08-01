package com.twoandahalfdevs.dr_improvement_mod.mixin;

import com.twoandahalfdevs.dr_improvement_mod.DrImprovementModKt;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
  @Shadow
  private Text overlayMessage;

  @Shadow
  private int overlayRemaining;

  @Unique
  private static int oldTime = 0;

  @Inject(method = "render", at = @At("HEAD"))
  private void renderHead(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
    if (this.overlayMessage != null) {
      DrImprovementModKt.setActionBarMsg(this.overlayMessage.getString());
    } else {
      DrImprovementModKt.setActionBarMsg(null);
    }
    DrImprovementModKt.setActionBarTime(this.overlayRemaining);

    oldTime = this.overlayRemaining;
  }

  @Inject(method = "render", at = @At("TAIL"))
  private void renderTail(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
    this.overlayRemaining = oldTime;
  }
}