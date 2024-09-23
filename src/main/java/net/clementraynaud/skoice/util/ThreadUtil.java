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

package net.clementraynaud.skoice.util;

import org.bukkit.Bukkit;

public final class ThreadUtil {

    private static final boolean DEBUG = false;

    private ThreadUtil() {
    }

    public static void ensureNotMainThread(boolean disablingBypass) {
        if (ThreadUtil.DEBUG && Bukkit.isPrimaryThread() && (!disablingBypass || Bukkit.getPluginManager().isPluginEnabled("Skoice"))) {
            Exception exception = new IllegalStateException("This method should not be called from the main thread.");
            exception.printStackTrace();
        }
    }

    public static void ensureNotMainThread() {
        ThreadUtil.ensureNotMainThread(false);
    }

}
