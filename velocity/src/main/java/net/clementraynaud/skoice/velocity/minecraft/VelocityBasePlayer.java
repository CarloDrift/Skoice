package net.clementraynaud.skoice.velocity.minecraft;

import com.velocitypowered.api.proxy.Player;
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.kyori.adventure.text.Component;

public class VelocityBasePlayer extends BasePlayer {

    private final Player player;

    public VelocityBasePlayer(Player player) {
        super(player.getUniqueId());
        this.player = player;
    }

    @Override
    public void sendMessage(Component message) {
        this.player.sendMessage(message);
    }

    @Override
    public void sendActionBar(Component message) {
        this.player.sendActionBar(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.hasPermission(permission);
    }
}
