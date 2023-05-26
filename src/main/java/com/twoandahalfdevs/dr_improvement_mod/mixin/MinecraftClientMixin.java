package com.twoandahalfdevs.dr_improvement_mod.mixin;

import com.mojang.blaze3d.framebuffer.SimpleFramebuffer;
import com.mojang.blaze3d.glfw.Window;
import com.twoandahalfdevs.dr_improvement_mod.DrImprovementMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
