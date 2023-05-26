package com.twoandahalfdevs.dr_improvement_mod.mixin;

import com.twoandahalfdevs.dr_improvement_mod.DrImprovementMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickingPacketListener;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements TickingPacketListener, ClientPlayPacketListener {
  @Shadow
  @Final
  private MinecraftClient client;

  @Inject(method = "onExperienceBarUpdate", at = @At("HEAD"), cancellable = true)
  private void experienceBarUpdateHead(ExperienceBarUpdateS2CPacket packet, CallbackInfo ci) {
    NetworkThreadUtils.forceMainThread(packet, this, this.client);
    DrImprovementMod.expUpdateTime = System.nanoTime();
    DrImprovementMod.prevExp = DrImprovementMod.latestExp;
    DrImprovementMod.latestExp = packet.getBarProgress();

    // Haha you're a mana bar now!
    this.client.player.setExperience((float) packet.getExperience() / 100f, packet.getExperienceLevel(), packet.getExperience());

    ci.cancel();
  }

  @Inject(method = "onEntityTrackerUpdate", at = @At("HEAD"))
  public void onEntityTrackerUpdateHead(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci) {
    if (client.world == null) return;
    var player = client.world.getEntityById(packet.id());
    if (player instanceof PlayerEntity) {
      var packetHealth = packet.trackedValues().stream().filter(
        i -> i.id() == 9
      ).findFirst();

      if (packetHealth.isPresent() && packetHealth.get().value() != null) {
        var health = (float) packetHealth.get().value();

        // 1.0 isn't a real health it's just like nothing like it means literally nothing it just means
        //  they're below 5% health it's useless I swear to god I hate this game it doesn't make any sense I just want it to make sense
        if (health != 1.0f) {
          // Update the health now : )
          DrImprovementMod.latestCurrentHealth.put(player.getEntityName(), health);
        }
      }
    }
  }

  @Inject(method = "onScoreboardPlayerUpdate", at = @At("HEAD"), cancellable = true)
  public void onScoreboardPlayerUpdateHead(ScoreboardPlayerUpdateS2CPacket packet, CallbackInfo ci) {
    if (!Objects.equals(packet.getObjectiveName(), "health") || client.world == null) {
      return;
    }

    DrImprovementMod.scoreWasUpdated.put(packet.getPlayerName(), packet.getScore());

    // In case the player is under 5% health, we need to display SOME info
    var player = client.world.getPlayers()
      .stream()
      .filter(p -> p.getEntityName().equals(packet.getPlayerName()))
      .findFirst();
    if (player.isEmpty()) return;

    var maxHealth = DrImprovementMod.maxHealthValues.get(packet.getPlayerName());
    if (maxHealth != null && player.get() != client.player) {
      player.get().setHealth(((float) packet.getScore() * 20f) / (float) maxHealth);
    }

    // We have to wait for an update haha
    DrImprovementMod.latestCurrentHealth.remove(packet.getPlayerName());

    // TODO - just update the score when we get some debug haha
    ci.cancel();
  }
}

