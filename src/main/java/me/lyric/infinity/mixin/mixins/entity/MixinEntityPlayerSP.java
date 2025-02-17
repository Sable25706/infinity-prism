package me.lyric.infinity.mixin.mixins.entity;

import com.mojang.authlib.GameProfile;
import me.lyric.infinity.Infinity;
import me.lyric.infinity.api.event.entity.LivingUpdateEvent;
import me.lyric.infinity.api.event.player.MotionUpdateEvent;
import me.lyric.infinity.api.event.player.MoveEvent;
import me.lyric.infinity.impl.modules.render.Swing;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static me.lyric.infinity.api.util.minecraft.IGlobals.mc;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
    private MotionUpdateEvent motionEvent;

    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }
    @Override
    public void move(@NotNull MoverType type, double x, double y, double z) {
        MoveEvent event = new MoveEvent(type, x, y, z);
        Infinity.INSTANCE.eventBus.post(event);
        super.move(type, event.getMotionX(), event.getMotionY(), event.getMotionZ());
    }
    @Inject(method = "onUpdateWalkingPlayer", at = @At(value = "HEAD"), cancellable = true)
    private void onUpdateWalkingPlayerHead(CallbackInfo callbackInfo) {
        motionEvent = new MotionUpdateEvent(1, this.posX, this.getEntityBoundingBox().minY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround);
        Infinity.INSTANCE.eventBus.post(motionEvent);
        if (motionEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }
    @Inject(method = "onUpdateWalkingPlayer", at = @At(value = "RETURN"))
    private void onUpdateWalkingPlayerReturn(CallbackInfo callbackInfo) {
        MotionUpdateEvent event = new MotionUpdateEvent(2, motionEvent);
        event.setCancelled(motionEvent.isCancelled());
        Infinity.INSTANCE.eventBus.post(event);
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setSprinting(Z)V", ordinal = 2))
    public void onLivingUpdate(EntityPlayerSP entityPlayerSP, boolean sprinting) {
        LivingUpdateEvent livingUpdateEvent = new LivingUpdateEvent(entityPlayerSP, sprinting);
        Infinity.INSTANCE.eventBus.post(livingUpdateEvent);

        if (livingUpdateEvent.isCancelled())
            livingUpdateEvent.getEntityPlayerSP().setSprinting(true);
        else
            entityPlayerSP.setSprinting(sprinting);
    }

    @Inject(method = "swingArm" , at =  @At("HEAD") , cancellable = true)
    public void swingArm(EnumHand enumHand, CallbackInfo info) {
        if (Swing.INSTANCE.isEnabled()) {
            if (Swing.INSTANCE.swing.getValue() == Swing.SwingHand.MAINHAND)
            {
                super.swingArm(EnumHand.MAIN_HAND);
            }
            if (Swing.INSTANCE.swing.getValue() == Swing.SwingHand.OFFHAND)
            {
                super.swingArm(EnumHand.OFF_HAND);
            }
            if(Swing.INSTANCE.swing.getValue() == Swing.SwingHand.NONE)
            {
                info.cancel();
            }
            Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketAnimation(enumHand));
            info.cancel();
        }
    }


}