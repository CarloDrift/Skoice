/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.tasks;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.storage.TempYamlFile;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.List;

public class UpdateVoiceStateTask {

    private final Skoice plugin;
    private final Member member;
    private final VoiceChannel channel;

    public UpdateVoiceStateTask(Skoice plugin, Member member, VoiceChannel channel) {
        this.plugin = plugin;
        this.member = member;
        this.channel = channel;
    }

    public void run() {
        if (this.member.getVoiceState() == null || this.plugin.getConfigYamlFile().getVoiceChannel() == null) {
            return;
        }
        boolean isMainVoiceChannel = this.channel.getId().equals(this.plugin.getConfigYamlFile().getVoiceChannel().getId());
        if (isMainVoiceChannel) {
            if (!this.member.getVoiceState().isGuildMuted()
                    && this.member.hasPermission(this.channel, Permission.VOICE_SPEAK, Permission.VOICE_MUTE_OTHERS)
                    && this.channel.getGuild().getSelfMember().hasPermission(this.channel, Permission.VOICE_MUTE_OTHERS)
                    && this.channel.getGuild().getSelfMember().hasPermission(this.plugin.getConfigYamlFile().getCategory(), Permission.VOICE_MOVE_OTHERS)) {
                this.member.mute(true).queue();
                List<String> mutedUsers = this.plugin.getTempYamlFile().getStringList(TempYamlFile.MUTED_USERS_ID_FIELD);
                if (!mutedUsers.contains(this.member.getId())) {
                    mutedUsers.add(this.member.getId());
                    this.plugin.getTempYamlFile().set(TempYamlFile.MUTED_USERS_ID_FIELD, mutedUsers);
                }
            }
        } else {
            VoiceChannel afkChannel = this.plugin.getBot().getGuild().getAfkChannel();
            if (afkChannel != null && this.channel.getId().equals(afkChannel.getId())) {
                return;
            }
            List<String> mutedUsers = this.plugin.getTempYamlFile().getStringList(TempYamlFile.MUTED_USERS_ID_FIELD);
            if (mutedUsers.contains(this.member.getId()) || this.member.hasPermission(Permission.VOICE_MUTE_OTHERS)) {
                this.member.mute(false).queue();
                mutedUsers.remove(this.member.getId());
                this.plugin.getTempYamlFile().set(TempYamlFile.MUTED_USERS_ID_FIELD, mutedUsers);
            }
        }
    }
}
