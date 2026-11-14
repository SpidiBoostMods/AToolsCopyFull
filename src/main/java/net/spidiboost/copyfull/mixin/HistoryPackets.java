package net.spidiboost.copyfull.mixin;
import net.spidiboost.copyfull.CopyClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=ClientPlayNetworkHandler.class,priority=1200)
abstract class HistoryPackets {
    @Inject(method="onGameMessage",at=@At("HEAD"))
    private void copyfull$receive(GameMessageS2CPacket packet, CallbackInfo ci) {
        var c=MinecraftClient.getInstance();
        if(c.isOnThread() && (Object)this==c.getNetworkHandler() && !packet.overlay() && CopyClient.active())
            CopyClient.receive(packet.content().getString());
    }
}
