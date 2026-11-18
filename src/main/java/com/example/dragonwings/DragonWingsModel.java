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

public class DragonWingsModel extends Model<PlayerEntityRenderState> {
    public static final EntityModelLayer LAYER =
            new EntityModelLayer(Identifier.of(DragonWingsClient.MOD_ID, "dragon_wings"), "main");

    private static final int FINGERS = 4;
    private static final float UPPER_ARM = 4.0f;
    private static final float FOREARM = 5.5f;
    private static final float[] FINGER_LENGTHS = {16.0f, 14.0f, 11.5f, 8.5f};

    private final Wing left;
    private final Wing right;

    private static class Wing {
        final float side;
        final ModelPart root;
        final ModelPart forearm;
        final ModelPart[] fingers = new ModelPart[FINGERS];
        final ModelPart[] membranes = new ModelPart[FINGERS - 1];

        Wing(ModelPart rootOfModel, String name, float side) {
            this.side = side;
            this.root = rootOfModel.getChild(name + "_wing");
            this.forearm = this.root.getChild("forearm");
            for (int i = 0; i != FINGERS; i++) {
                this.fingers[i] = this.forearm.getChild("finger" + i);
            }
            for (int i = 0; i != FINGERS - 1; i++) {
                this.membranes[i] = this.forearm.getChild("membrane" + i);
            }
        }
    }

    public DragonWingsModel(ModelPart root) {
        super(root, RenderLayers::entityCutoutNoCull);
        this.right = new Wing(root, "right", 1f);
        this.left = new Wing(root, "left", -1f);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        addWing(root, "right", 1f);
        addWing(root, "left", -1f);
        return TexturedModelData.of(data, 64, 64);
    }

    private static void addWing(ModelPartData root, String name, float s) {
        boolean isRight = Float.compare(s, 0f) == 1;

        // Tucked close onto the upper back
        ModelPartData wing = root.addChild(name + "_wing",
                ModelPartBuilder.create().uv(0, 32)
                        .cuboid(isRight ? 0 : -UPPER_ARM, -1.25f, -1.25f, UPPER_ARM, 2.5f, 2.5f),
                ModelTransform.origin(s * 1.5f, 2.5f, 2.0f));

        // Forearm creates the top peak of the bat/dragon wing
        ModelPartData forearm = wing.addChild("forearm",
                ModelPartBuilder.create().uv(0, 32)
                        .cuboid(isRight ? 0 : -FOREARM, -1.0f, -1.0f, FOREARM, 2.0f, 2.0f),
                ModelTransform.origin(s * UPPER_ARM, 0f, 0f));

        // Sharp curved wing-claw spike on the top peak
        ModelPartData clawBase = forearm.addChild("claw_base",
                ModelPartBuilder.create().uv(0, 32)
                        .cuboid(-0.6f, -3.2f, -0.6f, 1.2f, 3.2f, 1.2f),
                ModelTransform.of(s * FOREARM, -0.5f, 0f, 0.35f, 0f, s * 0.25f));

        clawBase.addChild("claw_tip",
                ModelPartBuilder.create().uv(0, 32)
                        .cuboid(-0.4f, -2.5f, -0.4f, 0.8f, 2.5f, 0.8f),
                ModelTransform.of(0f, -3.0f, 0f, 0.45f, 0f, -s * 0.35f));

        // Inner webbing connecting the forearm arch to the torso
        float innerX = isRight ? 0.5f : -(FOREARM + 0.5f);
        forearm.addChild("inner_membrane",
                ModelPartBuilder.create().uv(0, 0)
                        .cuboid(innerX, 0.2f, -0.25f, FOREARM, 7.5f, 0.5f),
                ModelTransform.NONE);

        // Tapered dragon finger bones (long at top, shorter toward the bottom)
        for (int i = 0; i != FINGERS; i++) {
            float len = FINGER_LENGTHS[i];
            float thickness = i == 0 ? 1.2f : 0.9f;
            float half = thickness * 0.5f;
            forearm.addChild("finger" + i,
                    ModelPartBuilder.create().uv(0, 32)
                            .cuboid(isRight ? 0 : -len, -half, -half, len, thickness, thickness),
                    ModelTransform.origin(s * FOREARM, 0f, 0f));
        }

        // Tapered membrane panels stretched between each pair of fingers
        for (int i = 0; i != FINGERS - 1; i++) {
            float memLen = (FINGER_LENGTHS[i] + FINGER_LENGTHS[i + 1]) * 0.5f - 1.5f;
            float x0 = isRight ? 0.5f : -(memLen + 0.5f);
            forearm.addChild("membrane" + i,
                    ModelPartBuilder.create().uv(0, 0)
                            .cuboid(x0, -3.2f, -0.25f, memLen, 6.4f, 0.5f),
                    ModelTransform.origin(s * FOREARM, 0f, 0f));
        }
    }

    @Override
    public void setAngles(PlayerEntityRenderState state) {
        super.setAngles(state);
        animate(this.right, state);
        animate(this.left, state);
    }

    private static void animate(Wing w, PlayerEntityRenderState state) {
        float s = w.side;
        float time = state.age;

        float move = MathHelper.clamp(state.limbSwingAmplitude, 0f, 1f);

        // Smooth, weighty dragon wingbeat
        float speed = 0.10f + 0.14f * move;
        float flap = MathHelper.sin(time * speed);
        float flapFollow = MathHelper.sin(time * speed - 0.65f);
        float flapSize = 0.08f + 0.30f * move;

        // Shoulder: tucked back at rest, flares open when moving, sweeps with each stroke
        float swingBack = 0.85f - 0.45f * move + flapFollow * 0.08f;
        w.root.yaw = -s * swingBack;
        // Negative roll on right wing lifts the shoulder upward into a dramatic arch
        w.root.roll = -s * (0.42f + flap * flapSize);
        w.root.pitch = 0.12f + flap * 0.05f;

        // Elbow: bends sharply downward from the high shoulder peak
        float elbowBend = 0.95f - flapFollow * flapSize * 0.75f;
        w.forearm.roll = s * elbowBend;

        // Fingers fan out into jagged dragon points with a trailing wave motion
        float spread = 0.38f + 0.14f * move;
        for (int i = 0; i != FINGERS; i++) {
            float angle = 0.15f + spread * i + flapFollow * flapSize * 0.25f * i;
            w.fingers[i].roll = s * angle;
        }

        // Each membrane sits centered between its two neighboring fingers
        for (int i = 0; i != FINGERS - 1; i++) {
            float a0 = 0.15f + spread * i + flapFollow * flapSize * 0.25f * i;
            float a1 = 0.15f + spread * (i + 1) + flapFollow * flapSize * 0.25f * (i + 1);
            w.membranes[i].roll = s * (a0 + a1) * 0.5f;
        }
    }
}
