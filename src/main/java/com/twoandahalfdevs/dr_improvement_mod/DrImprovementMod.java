package com.twoandahalfdevs.dr_improvement_mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.joml.Math;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DrImprovementMod implements ModInitializer, ClientTickEvents.StartTick {
  public static float latestExp = 0f;
  public static float prevExp = 0f;
  public static int prevSlot = 0;
  public static long expUpdateTime = 0L;

  public static Map<String, Integer> scoreWasUpdated = new ConcurrentHashMap<>();
  public static Map<String, Float> latestCurrentHealth = new ConcurrentHashMap<>();
  public static Map<String, Integer> maxHealthValues = new ConcurrentHashMap<>();

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
  public void onStartTick(MinecraftClient client) {
    if (client.world != null) {
      client.player.getDisplayName().getString();
      // Update health
      HashSet<String> toRemove = new HashSet<>();
      for (var entry : scoreWasUpdated.entrySet()) {
        var player = client.world.getPlayers()
          .stream()
          .filter(p -> p.getEntityName().equals(entry.getKey()))
          .findFirst();
        var health = latestCurrentHealth.getOrDefault(entry.getKey(), null);
        if (player.isPresent() && health != null) {
          var ratio = player.get().getMaxHealth() / health;
          var maxHealth = (double) entry.getValue() * ratio;
          if (!Double.isNaN(maxHealth)) {
            maxHealthValues.put(player.get().getEntityName(), (int) Math.round(maxHealth));
          }
          toRemove.add(entry.getKey());
        }
      }
      for (var e : toRemove) {
        scoreWasUpdated.remove(e);
      }

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

  @Override
  public void onInitialize() {
    ClientTickEvents.START_CLIENT_TICK.register(this);
  }
}
