package dev.hephaestus.esther.mixin;

import com.mojang.authlib.GameProfile;
import dev.hephaestus.esther.Esther;
import dev.hephaestus.esther.EstherDimensions;
import dev.hephaestus.esther.util.ImprintManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "wakeUp", at = @At("TAIL"))
    public void wakeUpHook(boolean bl, boolean bl2, CallbackInfo ci) {
        Esther.COMPONENT.get(this).resetMana();
    }

    @Inject(method = "onDeath", at = @At(value = "HEAD"), cancellable = true)
    public void onDeathDont(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity player = ((ServerPlayerEntity)(Object)this);
        if (player.dimension == EstherDimensions.IMPRINT) {
            BlockPos pos = ImprintManager.getInstance(player.getServerWorld()).getClosestImprint(player.getBlockPos()).getCenter();
            player.requestTeleport(pos.getX(), pos.getY(), pos.getZ());
            player.setHealth(1);
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void blazesDontLikeWaterMan(CallbackInfo ci) {
        if (this.touchingWater && Esther.COMPONENT.get(this).canFly()) {
            Esther.COMPONENT.get(this).setCanFly(false);
            this.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 1.0f, 0.0f);
        }
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
