/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.mobeffects.library;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.JsonUtils;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.library.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class Libraries {
    private Libraries() {
    }

    public static void initialize() {
        EffectLibrary.initialize();
        ItemLibrary.initialize();
        FootstepLibrary.initialize();

        // Get a list of the mods/packs that are installed
        final List<String> installed = ForgeUtils.getModIdList();

        for (final String id : installed) {
            try {
                final String resource = String.format("data/%s.json", id);
                final ModConfig mod = JsonUtils.load(new ResourceLocation(MobEffects.MOD_ID, resource), ModConfig.class);
                FootstepLibrary.initFromConfig(mod);
                ItemLibrary.initFromConfig(mod);
            } catch (@Nonnull final Throwable t) {
                MobEffects.LOGGER.error(t, "Unable to load '%s.json' config data!", id);
            }
        }
    }

    public static void complete() {
        ItemLibrary.complete();
    }
}
