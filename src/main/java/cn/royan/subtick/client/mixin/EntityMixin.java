package cn.royan.subtick.client.mixin;

import cn.royan.subtick.client.interfaces.IEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements IEntity {
    @Unique
    private boolean cGlowing = false;

    @Override
    @Unique
    public void setCGlowing(boolean value) {
        cGlowing = value;
    }

    @Inject(
            method = "isGlowing",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    private void isGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (cGlowing)
            cir.setReturnValue(true);
    }
}
