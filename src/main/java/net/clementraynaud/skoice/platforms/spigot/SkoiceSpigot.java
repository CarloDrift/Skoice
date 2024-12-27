package net.clementraynaud.skoice.platforms.spigot;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.model.minecraft.FullPlayer;
import net.clementraynaud.skoice.api.SkoiceAPI;
import net.clementraynaud.skoice.platforms.spigot.commands.skoice.SkoiceCommandSpigot;
import net.clementraynaud.skoice.platforms.spigot.logger.JULLoggerAdapter;
import net.clementraynaud.skoice.platforms.spigot.minecraft.SpigotBasePlayer;
import net.clementraynaud.skoice.platforms.spigot.minecraft.SpigotFullPlayer;
import net.clementraynaud.skoice.platforms.spigot.scheduler.SpigotTaskScheduler;
import net.clementraynaud.skoice.platforms.spigot.storage.SpigotLinksYamlFile;
import net.clementraynaud.skoice.platforms.spigot.system.SpigotListenerManager;
import net.clementraynaud.skoice.storage.LinksYamlFile;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.GameMode;
import org.bukkit.generator.WorldInfo;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SkoiceSpigot extends Skoice {

    private static final String OUTDATED_MINECRAFT_SERVER_ERROR_MESSAGE = "Skoice only supports Minecraft 1.8 or later. Please update your Minecraft server to use the proximity voice chat.";
    private final SkoicePluginSpigot plugin;
    private static BukkitAudiences adventure;
    private static SkoiceAPI api;

    public SkoiceSpigot(SkoicePluginSpigot plugin) {
        super(new JULLoggerAdapter(plugin.getLogger()), new SpigotTaskScheduler(plugin));
        super.setListenerManager(new SpigotListenerManager(this));
        this.plugin = plugin;
    }

    public static SkoiceAPI api() {
        return api;
    }

    @Override
    public void onEnable() {
        if (!this.isMinecraftServerCompatible()) {
            this.getLogger().severe(OUTDATED_MINECRAFT_SERVER_ERROR_MESSAGE);
            this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
            return;
        }
        api = new SkoiceAPI(this);
        this.adventure = BukkitAudiences.create(this.plugin);
        super.onEnable();
    }

    @Override
    public Bot createBot() {
        return new SpigotBot(this);
    }

    @Override
    public LinksYamlFile createLinksYamlFile() {
        return new SpigotLinksYamlFile(this);
    }

    @Override
    public void onDisable() {
        this.adventure.close();
        if (this.adventure != null) {
            super.onDisable();
        }
    }

    public static BukkitAudiences adventure() {
        return adventure;
    }

    private boolean isMinecraftServerCompatible() {
        try {
            GameMode.SPECTATOR.toString();
        } catch (NoSuchFieldError exception) {
            return false;
        }
        return true;
    }

    @Override
    public SkoiceCommand setSkoiceCommand() {
        return new SkoiceCommandSpigot(this);
    }

    @Override
    public boolean isEnabled() {
        return this.plugin.isEnabled();
    }

    @Override
    public File getDataFolder() {
        return this.plugin.getDataFolder();
    }

    @Override
    public BasePlayer getPlayer(UUID uuid) {
        return Optional.ofNullable(this.plugin.getServer().getPlayer(uuid)).map(SpigotBasePlayer::new).orElse(null);
    }

    @Override
    public Collection<FullPlayer> getOnlinePlayers() {
        return this.plugin.getServer().getOnlinePlayers().stream()
                .map(SpigotFullPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getWorlds() {
        return this.plugin.getServer().getWorlds().stream().map(WorldInfo::getName).toList();
    }

    @Override
    public FullPlayer getFullPlayer(BasePlayer player) {
        return Optional.ofNullable(this.plugin.getServer().getPlayer(player.getUniqueId())).map(SpigotFullPlayer::new).orElse(null);
    }

    public SkoicePluginSpigot getPlugin() {
        return this.plugin;
    }
}
