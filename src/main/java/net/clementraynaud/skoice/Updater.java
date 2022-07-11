/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 *
 * This file is part of Skoice.
 *
 * Skoice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skoice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skoice.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.clementraynaud.skoice;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class Updater {

    private final Skoice plugin;
    private final int resourceId;

    public Updater(Skoice plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void checkVersion() {
        this.getVersion(version -> {
            if (!this.plugin.getDescription().getVersion().equals(version)) {
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.outdated-version",
                        this.plugin.getDescription().getVersion(), version));
            }
        });
    }

    public void checkVersionInGame(Player player) {
        this.getVersion(version -> {
            if (!this.plugin.getDescription().getVersion().equals(version)) {
                player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.outdated-version",
                        this.plugin.getDescription().getVersion(), version));
            }
        });
    }

    private void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId)
                    .openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException ignored) {
            }
        });
    }
}
