/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.system;

import java.util.Comparator;
import java.util.Set;

public enum ActionBarAlert {

    CONNECTING(1),
    DISCONNECTING(1);

    private final int priority;
    private final String lowerCaseName;

    ActionBarAlert(int priority) {
        this.priority = priority;
        this.lowerCaseName = this.name().toLowerCase() + "-alert";
    }

    private int getPriority() {
        return this.priority;
    }

    @Override
    public String toString() {
        return this.lowerCaseName;
    }

    public static ActionBarAlert getPriorityAlert(Set<ActionBarAlert> alerts) {
        return alerts.stream()
                .max(Comparator.comparingInt(ActionBarAlert::getPriority))
                .orElse(null);
    }
}
