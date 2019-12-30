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

package org.orecruncher.mobeffects;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.mobeffects.library.EffectLibrary;
import org.orecruncher.mobeffects.library.FootstepLibrary;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = MobEffects.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class KeyBoard {
    private KeyBoard() {

    }

    private static KeyBinding RELOAD;

    public static void initialize() {
        if (Config.CLIENT.logging.enableLogging.get()) {
            RELOAD = new KeyBinding("Reload", GLFW.GLFW_KEY_F12, "Debug");
            ClientRegistry.registerKeyBinding(RELOAD);
        }
    }

    @SubscribeEvent
    public static void onKeyPress(@Nonnull final  TickEvent.ClientTickEvent evt) {

        if (RELOAD != null && evt.phase == TickEvent.Phase.END && GameUtils.isInGame() && RELOAD.isPressed()) {
            FootstepLibrary.initialize();
            EffectLibrary.initialize();
        }

    }
}
