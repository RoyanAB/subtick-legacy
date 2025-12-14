package cn.royan.subtick.client.mixin;

import cn.royan.subtick.client.ClientTickHandler;
import cn.royan.subtick.client.interfaces.ITickTimer;
import net.minecraft.client.TickTimer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TickTimer.class)
public abstract class TickTimerMixin implements ITickTimer {
    @Shadow
    private float mspt;

    @Inject(
            method = "advance",
            at = @At(
                    "HEAD"
            )
    )
    public void advance(CallbackInfo ci) {
        if (true) {
            if (!ClientTickHandler.frozen) {
                this.mspt = Math.max(50.0f, ClientTickHandler.mspt);
            }
        } else
            this.mspt = 50.0f;
    }

    @Unique
    @Override
    public void reset() {
        this.mspt = 50.0f;
    }
}
