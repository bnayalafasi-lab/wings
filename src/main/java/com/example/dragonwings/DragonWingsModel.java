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
    private static final float UPPER_ARM = 2.5f;
    private static final float FOREARM = 2.5f;
    private static final float FINGER_LEN = 14f;

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

        ModelPartData wing = root.addChild(name + "_wing",
                ModelPartBuilder.create().uv(0, 32).cuboid(isRight ? 0 : -UPPER_ARM, -1f, -1f, UPPER_ARM, 2f, 2f),
                ModelTransform.origin(s * 1.5f, 2.5f, 2.0f));

        ModelPartData forearm = wing.addChild("forearm",
                ModelPartBuilder.create().uv(0, 32).cuboid(isRight ? 0 : -FOREARM, -1f, -1f, FOREARM, 2f, 2f),
                ModelTransform.origin(s * UPPER_ARM, 0f, 0f));

        for (int i = 0; i != FINGERS; i++) {
            forearm.addChild("finger" + i,
                    ModelPartBuilder.create().uv(0, 32).cuboid(isRight ? 0 : -FINGER_LEN, -0.5f, -0.5f, FINGER_LEN, 1f, 1f),
                    ModelTransform.origin(s * FOREARM, 0f, 0f));
        }
        for (int i = 0; i != FINGERS - 1; i++) {
            float x0 = isRight ? 0.5f : -12.5f;
            forearm.addChild("membrane" + i,
                    ModelPartBuilder.create().uv(0, 0).cuboid(x0, -3f, -0.25f, 12f, 6f, 0.5f),
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

        float flap = MathHelper.sin(time * (0.12f + 0.12f * move));
        float flapSize = 0.06f + 0.28f * move;

        float swingBack = 1.0f - 0.55f * move + flap * 0.05f;
        w.root.yaw = -s * swingBack;
        w.root.roll = -s * (0.15f + flap * flapSize);

        float elbowUp = 0.55f - flap * flapSize * 0.8f;
        w.forearm.roll = -s * elbowUp;

        float spread = 0.85f + 0.3f * move;
        for (int i = 0; i != FINGERS; i++) {
            float angle = spread * (0.5f + 0.5f * i) + flap * flapSize * 0.35f * i;
            w.fingers[i].roll = s * angle;
        }
        for (int i = 0; i != FINGERS - 1; i++) {
            float a0 = spread * (0.5f + 0.5f * i) + flap * flapSize * 0.35f * i;
            float a1 = spread * (0.5f + 0.5f * (i + 1)) + flap * flapSize * 0.35f * (i + 1);
            w.membranes[i].roll = s * (a0 + a1) * 0.5f;
        }
    }
}
