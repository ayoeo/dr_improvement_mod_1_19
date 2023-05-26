package com.twoandahalfdevs.dr_improvement_mod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class ItemRendererMixin {
  @Shadow
  private float equipProgressMainHand;

  @Shadow
  private float equipProgressOffHand;

  @Shadow
  private float prevEquipProgressMainHand;

  @Shadow
  private float prevEquipProgressOffHand;

  @Shadow
  private ItemStack mainHand;

  @Shadow
  private ItemStack offHand;

  @Shadow
  @Final
  private MinecraftClient client;

  @Inject(method = "updateHeldItems", at = @At("TAIL"))
  private void modifyEquipProgress(CallbackInfo ci) {
    // No more annoying held item visual equip time reset when hitting stuff
    mainHand = client.player.getMainHandStack();
    offHand = client.player.getOffHandStack();
    equipProgressMainHand = prevEquipProgressMainHand = 1;
    equipProgressOffHand = prevEquipProgressOffHand = 1;
  }
}

