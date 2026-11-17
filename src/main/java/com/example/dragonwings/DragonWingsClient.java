package com.example.dragonwings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.EntityType;

public class DragonWingsClient implements ClientModInitializer {
    public static final String MOD_ID = "dragonwings";

    @Override
    @SuppressWarnings("unchecked")
    public void onInitializeClient() {
        // Tell the game about our wing model shape.
        EntityModelLayerRegistry.registerModelLayer(DragonWingsModel.LAYER, DragonWingsModel::getTexturedModelData);

        // Attach the wings to every player model.
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == EntityType.PLAYER) {
                var wingModel = new DragonWingsModel(context.getEntityModels().getModelPart(DragonWingsModel.LAYER));
                registrationHelper.register(new DragonWingsFeatureRenderer(
                        (FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel>) entityRenderer,
                        wingModel));
            }
        });
    }
}
