package com.example.dragonwings;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Two dragon wings. Each wing is: upper arm -> forearm -> 4 finger bones with
 * membrane panels stretched between them.
 *
 * Note: in Minecraft model space +Y points DOWN, so "up" is negative Y.
 */
public class DragonWingsModel extends Model<PlayerEntityRenderState> {
    public static final EntityModelLayer LAYER =
            new EntityModelLayer(Identifier.of(DragonWingsClient.MOD_ID, "dragon_wings"), "main");

    private static final int FINGERS = 4;
    private static final float UPPER_ARM = 9f;
    private static final float FOREARM = 10f;
    private static final float FINGER_LEN = 14f;

    private final Wing left;
    private final Wing right;

    /** Handles to the moving pieces of one wing. */
    private static class Wing {
        final float side; // +1 right, -1 left
        final ModelPart root;
        final ModelPart forearm;
        final ModelPart[] fingers = new ModelPart[FINGERS];
        final ModelPart[] membranes = new ModelPart[FINGERS - 1];

        Wing(ModelPart rootOfModel, String name, float side) {
            this.side = side;
            this.root = rootOfModel.getChild(name + "_wing");
            this.forearm = this.root.getChild("forearm");
            for (int i = 0; i  0 ? 0 : -UPPER_ARM, -1f, -1f, UPPER_ARM, 2f, 2f),
                ModelTransform.origin(s * 2.5f, 2f, 2.5f));

        // Elbow.
        ModelPartData forearm = wing.addChild("forearm",
                ModelPartBuilder.create().uv(0, 32).cuboid(s > 0 ? 0 : -FOREARM, -1f, -1f, FOREARM, 2f, 2f),
                ModelTransform.origin(s * UPPER_ARM, 0f, 0f));

        // Finger bones and the membranes between them. Both start at the wrist.
        // Angles are set every frame in setAngles(), so they start straight here.
        for (int i = 0; i < FINGERS; i++) {
            forearm.addChild("finger" + i,
                    ModelPartBuilder.create().uv(0, 32).cuboid(s > 0 ? 0 : -FINGER_LEN, -0.5f, -0.5f, FINGER_LEN, 1f, 1f),
                    ModelTransform.origin(s * FOREARM, 0f, 0f));
        }
        for (int i = 0; i < FINGERS - 1; i++) {
            // 12 long, 6 wide, 0.5 thick, starting 2 px away from the wrist.
            float x0 = s > 0 ? 2f : -14f;
            forearm.addChild("membrane" + i,
                    ModelPartBuilder.create().uv(0, 0).cuboid(x0, -3f, -0.25f, 12f, 6f, 0.5f),
                    ModelTransform.origin(s * FOREARM, 0f, 0f));
        }
    }

    // ------------------------------------------------------------------
    // Animation
    // ------------------------------------------------------------------

    @Override
    public void setAngles(PlayerEntityRenderState state) {
        super.setAngles(state);
        animate(this.right, state);
        animate(this.left, state);
    }

    private static void animate(Wing w, PlayerEntityRenderState state) {
        float s = w.side;
        float time = state.age;

        // 0 = standing still, 1 = moving fast.
        float move = MathHelper.clamp(state.limbAmplitudeMultiplier, 0f, 1f);

        // Slow flutter when idle, faster and bigger when moving.
        float flap = MathHelper.sin(time * (0.12f + 0.12f * move));
        float flapSize = 0.06f + 0.28f * move;

        // Shoulder: swing back against the body at rest, spread out when moving.
        float swingBack = 1.0f - 0.55f * move + flap * 0.05f;
        w.root.yaw = -s * swingBack;
        // Shoulder: lift up and down.
        w.root.roll = -s * (0.15f + flap * flapSize);

        // Elbow: bends up, then folds in when the wing flaps down.
        float elbowUp = 0.7f - flap * flapSize * 0.8f;
        w.forearm.roll = -s * elbowUp;

        // Fingers fan out downwards from the wrist, more when moving.
        float spread = 0.85f + 0.3f * move;
        for (int i = 0; i < FINGERS; i++) {
            float angle = spread * (0.5f + 0.5f * i) + flap * flapSize * 0.35f * i;
            w.fingers[i].roll = s * angle;
        }
        // Each membrane sits halfway between two neighbouring fingers.
        for (int i = 0; i < FINGERS - 1; i++) {
            float a0 = spread * (0.5f + 0.5f * i) + flap * flapSize * 0.35f * i;
            float a1 = spread * (0.5f + 0.5f * (i + 1)) + flap * flapSize * 0.35f * (i + 1);
            w.membranes[i].roll = s * (a0 + a1) * 0.5f;
        }
    }
}
