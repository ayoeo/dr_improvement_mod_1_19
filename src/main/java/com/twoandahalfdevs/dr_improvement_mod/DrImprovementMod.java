package com.twoandahalfdevs.dr_improvement_mod;

import com.mojang.blaze3d.framebuffer.Framebuffer;
import com.mojang.blaze3d.framebuffer.SimpleFramebuffer;
import net.minecraft.client.MinecraftClient;
import org.joml.Math;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ClientOnly
public class DrImprovementMod implements ClientModInitializer, ClientTickEvents.Start {
  public static float latestExp = 0f;
  public static float prevExp = 0f;
  public static long expUpdateTime = 0L;

  public static Map<String, Integer> scoreWasUpdated = new HashMap<>();
  public static Map<String, Float> latestCurrentHealth = new HashMap<>();
  public static Map<String, Integer> maxHealthValues = new HashMap<>();

  public static Framebuffer nametagFb;

  public static float interpolatedExp() {
    float expDelta = latestExp - prevExp;

    float deltaTimeMs = (System.nanoTime() - expUpdateTime) / 1000000f;
    // If we're going up, interpolate slower
    if (expDelta > 0f) {
      return prevExp + expDelta * Math.clamp(0f, 1f, deltaTimeMs / 50f);
    } else {
      return prevExp + expDelta * Math.clamp(0f, 1f, deltaTimeMs / 10f);
    }
  }

  @Override
  public void onInitializeClient(ModContainer mod) {

  }

  @Override
  public void startClientTick(MinecraftClient client) {
    if (client.world != null) {
      // Update health
      scoreWasUpdated = scoreWasUpdated
        .entrySet()
        .stream()
        .filter(entry -> {
          var player = client.world.getPlayers()
            .stream()
            .filter(p -> {
              return p.getEntityName().equals(entry.getKey());
            })
            .findFirst();
          var health = latestCurrentHealth.getOrDefault(entry.getKey(), null);
          if (player.isPresent() && health != null) {
            var ratio = player.get().getMaxHealth() / health;
            var maxHealth = (double) entry.getValue() * ratio;
            if (!Double.isNaN(maxHealth)) {
              maxHealthValues.put(player.get().getEntityName(), (int) Math.round(maxHealth));
            }
            return false;
          } else {
            return true;
          }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      // Update the scoreboard to reflect real health values
      var scoreboard = client.world.getScoreboard();
      var healthObjective = scoreboard.getObjective("health");
      if (healthObjective == null) return;
      var scores = scoreboard.getAllPlayerScores(healthObjective);
      for (var score : scores) {
        var maxHealth = maxHealthValues.get(score.getPlayerName());
        if (maxHealth != null) {
          var player = client.world.getPlayers()
            .stream()
            .filter(p -> p.getEntityName().equals(score.getPlayerName()))
            .findFirst();

          if (player.isPresent()) {
            var ratio = player.get().getHealth() / player.get().getMaxHealth();
            score.setScore((int) Math.round((double) maxHealth * ratio));
          }
        }
      }
    }
  }
}
