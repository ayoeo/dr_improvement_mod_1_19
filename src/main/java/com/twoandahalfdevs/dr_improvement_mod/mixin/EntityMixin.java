package com.twoandahalfdevs.dr_improvement_mod.mixin;

import com.twoandahalfdevs.dr_improvement_mod.ModConfig;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
  @Inject(method = "getTeamColorValue", at = @At(value = "RETURN"), cancellable = true)
  private void colorBlindGlowTime(CallbackInfoReturnable<Integer> cir) {
    if (!ModConfig.INSTANCE.getSettings().getColorBlind()) return;

    var color = cir.getReturnValue();
    switch (color) {
      case 16777045:
        cir.setReturnValue(0xff45a5);
        break;
      case 5636095:
        cir.setReturnValue(0x454bff);
        break;
      case 5635925:
        cir.setReturnValue(0x3b632f);
        break;
      case 16755200:
        cir.setReturnValue(0xff0303);
        break;
    }
  }
}
