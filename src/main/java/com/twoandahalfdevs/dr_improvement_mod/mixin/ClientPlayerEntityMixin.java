package com.twoandahalfdevs.dr_improvement_mod.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity {
  @Shadow
  protected int ticksLeftToDoubleTapSprint;

  public ClientPlayerEntityMixin(World world, BlockPos pos, float f, GameProfile gameProfile) {
    super(world, pos, f, gameProfile);
  }

  @Inject(method = "tickMovement", at = @At("HEAD"))
  private void tickMovementHead(CallbackInfo ci) {
    // No w-tap sprint
    this.ticksLeftToDoubleTapSprint = 0;
  }

  @Redirect(method = "dropSelectedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropSelectedItem(Z)Lnet/minecraft/item/ItemStack;"))
  private ItemStack dropSelectedItem(PlayerInventory instance, boolean entireStack) {
    // Don't do client-side drops anymore
    return this.getInventory().getMainHandStack();
  }


  @Inject(method = "dropSelectedItem", at = @At(value = "RETURN"), cancellable = true)
  private void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
    // Don't swing hand when dropping items
    cir.setReturnValue(false);
  }
}

