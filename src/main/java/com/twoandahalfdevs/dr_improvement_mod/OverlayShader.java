package com.twoandahalfdevs.dr_improvement_mod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class OverlayShader {
  private static final int overlayProgram;

  private static final int energyUniform;
  private static final int healthUniform;
  private static final int manaUniform;
  private static final int aspectRatioUniform;

  static {
    try {
      var vertShader = loadShader("overlay.vsh", true);
      var fragShader = loadShader("overlay.fsh", false);

      // Load program
      overlayProgram = GL20.glCreateProgram();
      GL20.glAttachShader(overlayProgram, vertShader);
      GL20.glAttachShader(overlayProgram, fragShader);
      GL20.glLinkProgram(overlayProgram);

      // Load uniforms
      energyUniform = GL20.glGetUniformLocation(overlayProgram, "energyPercent");
      healthUniform = GL20.glGetUniformLocation(overlayProgram, "healthPercent");
      manaUniform = GL20.glGetUniformLocation(overlayProgram, "manaPercent");
      aspectRatioUniform = GL20.glGetUniformLocation(overlayProgram, "aspectRatio");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static int loadShader(String file, boolean vertex) throws IOException {
    Identifier identifier = new Identifier("shaders/program/" + file);
    var resourceManager = MinecraftClient.getInstance().getResourceManager();
    Resource resource = resourceManager.getResourceOrThrow(identifier);
    InputStream inputStream = resource.getInputStream();

    String string = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    if (string == null) {
      throw new IOException("Could not load program " + file);
    } else {
      int i = GL20.glCreateShader(vertex ? GL20.GL_VERTEX_SHADER : GL20.GL_FRAGMENT_SHADER);
      GL20.glShaderSource(i, string);
      GL20.glCompileShader(i);
      if (GL20.glGetShaderi(i, 35713) == 0) {
        String string2 = StringUtils.trim(GL20.glGetShaderInfoLog(i, 32768));
        throw new IOException("Couldn't compile " + file + ": " + string2);
      } else {
        return i;
      }
    }
  }

  public static void draw() {
    var window = MinecraftClient.getInstance().getWindow();
    var player = MinecraftClient.getInstance().player;
    if (player == null) return;
    if (MinecraftClient.getInstance().options.hudHidden) return;

    int lastId = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
    GL20.glUseProgram(overlayProgram);

    // Update uniforms
    GL20.glUniform1f(energyUniform, DrImprovementModKt.interpolatedExp());
    GL20.glUniform1f(healthUniform, player.getHealth() / player.getMaxHealth());
    GL20.glUniform1f(manaUniform, player.experienceLevel / 100f);
    GL20.glUniform1f(aspectRatioUniform, (float) window.getFramebufferWidth() / window.getFramebufferHeight());

    // easy get draw
    RenderSystem.enableBlend();
    GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);

    GL20.glUseProgram(lastId);
  }
}
