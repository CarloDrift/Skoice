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

package net.clementraynaud.skoice.menus.selectmenus;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoiceChannelSelectMenu extends SelectMenu {

    private final Skoice plugin;

    public VoiceChannelSelectMenu(Skoice plugin) {
        super(plugin.getLang(), true);
        this.plugin = plugin;
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.selections.SelectMenu get() {
        List<VoiceChannel> voiceChannels = new ArrayList<>(this.plugin.getBot().getJDA().getVoiceChannels());
        List<Category> categories = new ArrayList<>();
        for (VoiceChannel voiceChannel : voiceChannels) {
            categories.add(voiceChannel.getParentCategory());
        }
        List<SelectOption> options = new ArrayList<>();
        options.add(SelectOption.of(super.lang.getMessage("discord.menu.voice-channel.select-menu.select-option.new-voice-channel.label"), "new-voice-channel")
                .withDescription(super.lang.getMessage("discord.menu.voice-channel.select-menu.select-option.new-voice-channel.description"))
                .withEmoji(MenuEmoji.HEAVY_PLUS_SIGN.get()));
        int optionIndex = 0;
        while (optionIndex < 23 && voiceChannels.size() > optionIndex) {
            if (voiceChannels.get(optionIndex).getParentCategory() != null) {
                options.add(SelectOption.of(voiceChannels.get(optionIndex).getName(), voiceChannels.get(optionIndex).getId())
                        .withDescription(categories.get(optionIndex).getName())
                        .withEmoji(MenuEmoji.SOUND.get()));
            }
            optionIndex++;
        }
        if (options.size() == 23) {
            options.add(SelectOption.of(super.lang.getMessage("discord.select-option.too-many-options.label"), "refresh")
                    .withDescription(super.lang.getMessage("discord.select-option.too-many-options.description"))
                    .withEmoji(MenuEmoji.WARNING.get()));
        }
        if (this.plugin.getBot().getStatus() == BotStatus.READY) {
            return net.dv8tion.jda.api.interactions.components.selections.SelectMenu.create("voice-channel-selection")
                    .addOptions(options)
                    .setDefaultValues(Collections.singleton(this.plugin.getConfiguration().getFile().getString(ConfigurationField.VOICE_CHANNEL_ID.toString()))).build();
        } else {
            return net.dv8tion.jda.api.interactions.components.selections.SelectMenu.create("voice-channel-selection")
                    .setPlaceholder(super.lang.getMessage("discord.menu.voice-channel.select-menu.placeholder"))
                    .addOptions(options).build();
        }
    }
}
