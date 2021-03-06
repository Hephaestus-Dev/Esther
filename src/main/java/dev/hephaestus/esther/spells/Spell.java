package dev.hephaestus.esther.spells;

import dev.hephaestus.esther.Esther;
import net.minecraft.entity.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;

public abstract class Spell {
    public enum Difficulty {
        TRIVIAL,
        EASY,
        MODERATE,
        HARD,
        IMPOSSIBLE
    }

    private final Difficulty difficulty;
    protected final Identifier id;
    protected final int cost;
    private SoundEvent sound;

    public Spell(Identifier id, Difficulty difficulty, int cost) {
        this.difficulty = difficulty;
        this.id = id;
        this.cost = cost;
    }

    public void cast(ServerPlayerEntity player) {
        Esther.COMPONENT.get(player).useMana(this.cost);
    }

    protected void fail(ServerPlayerEntity player) {
        Esther.log(player.getName().asString() + " failed to cast " + id);
        switch(this.difficulty) {
            // TODO: Add drawbacks for failed spell casts
        }
    }

    public boolean canCast(ServerPlayerEntity player) {
        return player.isCreative() || Esther.COMPONENT.get(player).getMana() >= this.cost;
    }

    public Spell withSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    public Identifier getId() {
        return this.id;
    }

    private static Vec3d getLooking(ServerPlayerEntity player) {
        float f = -MathHelper.sin(player.yaw * 0.017453292F) * MathHelper.cos(player.pitch * 0.017453292F);
        float g = -MathHelper.sin(player.pitch * 0.017453292F);
        float h = MathHelper.cos(player.yaw * 0.017453292F) * MathHelper.cos(player.pitch * 0.017453292F);

        return new Vec3d(f,g,h);
    }

    protected static BlockHitResult traceForBlock(ServerPlayerEntity player, int range) {

        return player.world.rayTrace(new RayTraceContext(
                player.getCameraPosVec(1.0f),
                player.getCameraPosVec(1.0f).add(getLooking(player).multiply(range)),
                RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, player
        ));
    }

    protected static EntityHitResult traceForEntity(ServerPlayerEntity player, int range) {
        Vec3d vec3d2 = player.getRotationVec(1.0F);

        return ProjectileUtil.getEntityCollision(
                player.world,
                null,
                player.getCameraPosVec(1.0f),
                player.getCameraPosVec(1.0f).add(getLooking(player).multiply(range)),
                player.getCameraEntity().getBoundingBox().stretch(vec3d2.multiply(range)).expand(1.0D),
                (entity) -> !entity.isSpectator() && entity.isAlive() && entity.collides()
        );
    }

    public int getCost() {
        return cost;
    }
}
