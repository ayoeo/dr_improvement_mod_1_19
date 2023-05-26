package com.twoandahalfdevs.dr_improvement_mod.mixin;

import com.twoandahalfdevs.dr_improvement_mod.DrImprovementMod;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
//  @Shadow
//  protected abstract void renderLabelIfPresent(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
//
//  @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"), cancellable = true)
//  private void preRenderLabel(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
//    ci.cancel();
//
//    // Use OUR framebuffer!!!
////    int current = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
//    int currentFb = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
////    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, DrImprovementMod.nametagFb.framebufferId);
//    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, DrImprovementMod.nametagFb.framebufferId);
//
////    GL20.glUseProgram(0);
//
//    // drawww
//    this.renderLabelIfPresent(entity, entity.getDisplayName(), matrices, vertexConsumers, light);
//
////    GL20.glUseProgram(current);
//    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, currentFb);
////    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, currentFb);
//  }
}
