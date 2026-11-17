package com.example.dragonwings;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** Draws the dragon wings on the player's back every frame. */
public class DragonWingsFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private static final Identifier TEXTURE =
            Identifier.of(DragonWingsClient.MOD_ID, "textures/entity/dragon_wings.png");

    private final DragonWingsModel model;

    public DragonWingsFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context,
                                      DragonWingsModel model) {
        super(context);
        this.model = model;
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                       PlayerEntityRenderState state, float yaw, float pitch) {
        if (state.invisible) {
            return;
        }

        matrices.push();
        // Follow the body: crouching, swimming and leaning move the wings too.
        this.getContextModel().body.applyTransform(matrices);

        this.model.setAngles(state);
        queue.submitModel(
                this.model, state, matrices,
                RenderLayers.entityCutoutNoCull(TEXTURE),
                light, OverlayTexture.DEFAULT_UV, state.outlineColor, null);

        matrices.pop();
    }
}
