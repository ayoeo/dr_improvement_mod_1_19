package com.twoandahalfdevs.dr_improvement_mod.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
//  @Shadow
//  @Final
//  private Window window;
//
//  @Inject(method = "onResolutionChanged", at = @At(value = "TAIL"))
//  private void resolutionChangedTail(CallbackInfo ci) {
//    if (DrImprovementMod.nametagFb == null) {
//      DrImprovementMod.nametagFb = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), true, false);
//      DrImprovementMod.nametagFb.setClearColor(0, 0, 0, 0);
//    } else {
//      DrImprovementMod.nametagFb.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), false);
//    }
//  }
}
