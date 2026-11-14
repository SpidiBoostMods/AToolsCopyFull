package net.spidiboost.copyfull.mixin;
import net.spidiboost.copyfull.CopyClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@org.spongepowered.asm.mixin.Pseudo
@Mixin(targets="com.rwstafftool.modules.mlmoder.CopyFullHist",remap=false)
abstract class CopyHook {
    @Inject(method="startRecording(Ljava/lang/String;Ljava/lang/String;Z)V",at=@At("HEAD"),cancellable=true)
    private void copyfull$start(String nick,String count,boolean staff,CallbackInfo ci) {
        ci.cancel();
        CopyClient.start(nick,count,staff);
    }
}
