package com.twoandahalfdevs.dr_improvement_mod.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
  @Shadow
  public abstract void send(Packet<?> packet);

  private ClientSettingsC2SPacket latestSettings;

  private RequestCommandCompletionsC2SPacket latestCompletion;

  private long lastSettingsTime = 0L;

  private long lastCompletionTime = 0L;

  @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
  private void sendHead(Packet<?> packet, CallbackInfo ci) {
    if (packet instanceof ClientSettingsC2SPacket) {
      if (packet == latestSettings) {
        // We're resending it's fine
        latestSettings = null;
      } else {
        latestSettings = (ClientSettingsC2SPacket) packet;
        ci.cancel();
      }
    } else if (packet instanceof RequestCommandCompletionsC2SPacket) {
      if (packet == latestCompletion) {
        // We're resending it's fine
        latestCompletion = null;
      } else {
        latestCompletion = (RequestCommandCompletionsC2SPacket) packet;
        ci.cancel();
      }
    }
  }


  @Inject(method = "tick", at = @At("HEAD"))
  private void tickHead(CallbackInfo ci) {
    if (latestSettings != null && System.currentTimeMillis() - lastSettingsTime > 1000L) {
      lastSettingsTime = System.currentTimeMillis();
      send(latestSettings);
    }
    if (latestCompletion != null && System.currentTimeMillis() - lastCompletionTime > 500L) {
      lastCompletionTime = System.currentTimeMillis();
      send(latestCompletion);
    }
  }
}

