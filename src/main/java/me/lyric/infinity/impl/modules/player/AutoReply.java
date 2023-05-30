package me.lyric.infinity.impl.modules.player;

import me.bush.eventbus.annotation.EventListener;
import me.bush.eventbus.annotation.ListenerPriority;
import me.lyric.infinity.Infinity;
import me.lyric.infinity.api.event.network.PacketEvent;
import me.lyric.infinity.api.module.Category;
import me.lyric.infinity.api.module.Module;
import me.lyric.infinity.api.setting.Setting;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.math.Vec3d;


/**
 * @author lyric
 */
public class AutoReply extends Module {
    private static AutoReply INSTANCE = new AutoReply();
    public static AutoReply getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AutoReply();
        }
        return INSTANCE;
    }
    private void setInstance() {
        INSTANCE = this;
    }
    public AutoReply()
    {
        super("AutoReply","Automatically replies your coords to people you have added.", Category.PLAYER);
    }

    public Setting<Boolean> ignoreY = register(new Setting("IgnoreY","Doesn't send your Y coordinate.", true));

    @EventListener(priority = ListenerPriority.LOW)
    public void onReceivePacket(PacketEvent.Receive e)  {
        if (!nullSafe()) {
            return;
        }
        if (e.getPacket() instanceof SPacketChat) {
            SPacketChat p = (SPacketChat) e.getPacket();
            String msg = p.getChatComponent().getUnformattedText();
            if (msg.contains("says: ") || msg.contains("whispers: ")) {
                String ign = msg.split(" ")[0];
                if (mc.player.getName() == ign) {
                    return;
                }
                if (Infinity.INSTANCE.friendManager.isFriend((ign))) {
                    String lowerCaseMsg = msg.toLowerCase();
                    if (lowerCaseMsg.contains("cord") || lowerCaseMsg.contains("coord") || lowerCaseMsg.contains("coords") || lowerCaseMsg.contains("cords") || lowerCaseMsg.contains("wya") || lowerCaseMsg.contains("where are you") || lowerCaseMsg.contains("where r u") || lowerCaseMsg.contains("where ru")) {
                        if (lowerCaseMsg.contains("discord") || lowerCaseMsg.contains("record")) {
                            return;
                        }
                        Vec3d pos = mc.player.getPositionVector();
                        mc.player.sendChatMessage("/msg " + ign + (" " + pos.x + "x " + (ignoreY.getValue() ? "" : pos.y + "y ") + pos.z + "z"));
                    }
                }
            }
        }
    }
}