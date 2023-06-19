package com.twoandahalfdevs.dr_improvement_mod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
  @Shadow
  @Final
  private MinecraftClient client;

  @Inject(method = "method_41934", at = @At("HEAD"), cancellable = true)
  private void eee(ClientPlayerEntity clientPlayerEntity, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> cir) {
    // Disable client-side block place for ability heads (weird on 1.19 idk)
    if (clientPlayerEntity.getStackInHand(hand).getTranslationKey().equals("block.minecraft.player_head")) {
      cir.setReturnValue(ActionResult.CONSUME);
    }
  }
}
