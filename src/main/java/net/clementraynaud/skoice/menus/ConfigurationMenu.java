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

package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.storage.TempFileStorage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class ConfigurationMenu {

    private final Skoice plugin;

    public ConfigurationMenu(Skoice plugin) {
        this.plugin = plugin;
    }

    public Message update() {
        Menu menu;
        if (this.plugin.getBot().getStatus() == BotStatus.MULTIPLE_GUILDS) {
            menu = this.plugin.getBot().getMenu("server");
        } else if (this.plugin.getBot().getStatus() == BotStatus.MISSING_PERMISSION) {
            menu = this.plugin.getBot().getMenu("permissions");
        } else if (this.plugin.getBot().getStatus() == BotStatus.NO_VOICE_CHANNEL) {
            menu = this.plugin.getBot().getMenu("voice-channel");
        } else if (this.plugin.getBot().getStatus() == BotStatus.NO_RADIUS) {
            menu = this.plugin.getBot().getMenu("mode");
        } else {
            menu = this.plugin.getBot().getMenu("settings");
        }
        return menu.build();
    }

    public String getMessageId() {
        Message message = this.retrieveMessage();
        if (message != null) {
            return message.getId();
        }
        return "";
    }

    public Message retrieveMessage() {
        if (!this.plugin.getTempFileStorage().getFile().contains(TempFileStorage.CONFIG_MENU_FIELD)) {
            return null;
        }
        Guild guild = this.plugin.getBot().getJDA().getGuildById(this.plugin.getTempFileStorage().getFile()
                .getString(TempFileStorage.CONFIG_MENU_FIELD + "." + TempFileStorage.GUILD_ID_FIELD));
        if (guild == null) {
            return null;
        }
        GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, this.plugin.getTempFileStorage().getFile()
                .getString(TempFileStorage.CONFIG_MENU_FIELD + "." + TempFileStorage.CHANNEL_ID_FIELD));
        if (channel == null) {
            return null;
        }
        try {
            return channel.retrieveMessageById(this.plugin.getTempFileStorage().getFile()
                    .getString(TempFileStorage.CONFIG_MENU_FIELD + "." + TempFileStorage.MESSAGE_ID_FIELD)).complete();
        } catch (ErrorResponseException e) {
            this.clearConfig();
        }
        return null;
    }

    public void delete() {
        Message message = this.retrieveMessage();
        if (message != null) {
            message.delete().queue();
        }
    }

    public void store(Message message) {
        this.plugin.getTempFileStorage().getFile()
                .set(TempFileStorage.CONFIG_MENU_FIELD + "." + TempFileStorage.GUILD_ID_FIELD, message.getGuild().getId());
        this.plugin.getTempFileStorage().getFile()
                .set(TempFileStorage.CONFIG_MENU_FIELD + "." + TempFileStorage.CHANNEL_ID_FIELD, message.getGuildChannel().getId());
        this.plugin.getTempFileStorage().getFile()
                .set(TempFileStorage.CONFIG_MENU_FIELD + "." + TempFileStorage.MESSAGE_ID_FIELD, message.getId());
        this.plugin.getTempFileStorage().saveFile();
    }

    public void clearConfig() {
        this.plugin.getTempFileStorage().getFile().set(TempFileStorage.CONFIG_MENU_FIELD, null);
        this.plugin.getTempFileStorage().saveFile();
    }
}
