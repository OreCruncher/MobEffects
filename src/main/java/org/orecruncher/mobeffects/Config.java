/*
 * Dynamic Surroundings: Mob Effects
 * Copyright (C) 2019  OreCruncher
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.mobeffects;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.orecruncher.mobeffects.footsteps.FootprintStyle;

import javax.annotation.Nonnull;
import java.io.File;

@Mod.EventBusSubscriber(modid = MobEffects.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    @Nonnull
    public static final Client CLIENT;
    private static final String CLIENT_CONFIG = MobEffects.MOD_ID + File.separator + MobEffects.MOD_ID + "-client.toml";
    @Nonnull
    private static final ForgeConfigSpec clientSpec;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    private Config() {
    }

    private static void applyConfig() {
        CLIENT.update();
        MobEffects.LOGGER.setDebug(Config.CLIENT.logging.get_enableLogging());
        MobEffects.LOGGER.setTraceMask(Config.CLIENT.logging.get_flagMask());
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        applyConfig();
        MobEffects.LOGGER.debug("Loaded config file %s", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.ConfigReloading configEvent) {
        MobEffects.LOGGER.debug("Config file changed %s", configEvent.getConfig().getFileName());
        applyConfig();
    }

    public static void setup() {
        // The subdir with the mod ID name should have already been created
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec, CLIENT_CONFIG);
    }

    public static class Client {

        @Nonnull
        public final Logging logging;
        @Nonnull
        public final Footsteps footsteps;
        @Nonnull
        public final Effects effects;

        Client(@Nonnull final ForgeConfigSpec.Builder builder) {
            this.logging = new Logging(builder);
            this.footsteps = new Footsteps(builder);
            this.effects = new Effects(builder);
        }

        void update() {
            this.logging.update();
            this.footsteps.update();
            this.effects.update();
        }

        public static class Logging {

            private final BooleanValue enableLogging;
            private final BooleanValue onlineVersionCheck;
            private final IntValue flagMask;

            private boolean _enableLogging;
            private boolean _onlineVersionCheck;
            private int _flagMask;

            Logging(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Defines how logging will behave")
                        .push("Logging Options");

                this.enableLogging = builder
                        .comment("Enables/disables debug logging of the mod")
                        .translation("mobeffects.cfg.logging.EnableDebug")
                        .define("Debug Logging", false);

                this.onlineVersionCheck = builder
                        .comment("Enables/disables display of version check information")
                        .translation("mobeffects.cfg.logging.VersionCheck")
                        .define("Online Version Check Result", true);

                this.flagMask = builder
                        .comment("Bitmask for toggling various debug traces")
                        .translation("mobeffects.cfg.logging.FlagMask")
                        .defineInRange("Debug Flag Mask", 0, 0, Integer.MAX_VALUE);

                builder.pop();
            }

            void update() {
                this._enableLogging = this.enableLogging.get();
                this._onlineVersionCheck = this.onlineVersionCheck.get();
                this._flagMask = this.flagMask.get();
            }

            public boolean get_enableLogging() {
                return this._enableLogging;
            }

            public boolean get_onlineVersionCheck() {
                return this._onlineVersionCheck;
            }

            public int get_flagMask() {
                return this._flagMask;
            }
        }

        public static class Footsteps {

            private final BooleanValue firstPersonFootstepCadence;
            private final ForgeConfigSpec.EnumValue<FootprintStyle> playerFootprintStyle;
            private final BooleanValue footstepsAsQuadruped;

            private boolean _firstPersonFootstepCadence;
            private FootprintStyle _playerFootprintStyle = FootprintStyle.LOWRES_SQUARE;
            private boolean _footstepsAsQuadruped;

            public Footsteps(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Defines footstep effect generation")
                        .push("Footstep Options");

                this.firstPersonFootstepCadence = builder
                        .comment("Use first person footstep cadence")
                        .translation("mobeffects.cfg.footsteps.Cadence")
                        .define("First Person Cadence", false);

                this.playerFootprintStyle = builder
                        .comment("Style of footprint to display for a player")
                        .translation("mobeffects.cfg.footsteps.PlayerStyle")
                        .defineEnum("Player Footprint Style", FootprintStyle.LOWRES_SQUARE);

                this.footstepsAsQuadruped = builder
                        .comment("Generate footsteps as a quadruped (horse)")
                        .translation("mobeffects.cfg.footsteps.Quadruped")
                        .define("Footsteps as Quadruped", false);

                builder.pop();
            }

            void update() {
                this._firstPersonFootstepCadence = this.firstPersonFootstepCadence.get();
                this._footstepsAsQuadruped = this.footstepsAsQuadruped.get();
                this._playerFootprintStyle = this.playerFootprintStyle.get();
            }

            public boolean get_firstPersonFootstepCadence() {
                return this._firstPersonFootstepCadence;
            }

            public boolean get_footstepsAsQuadruped() {
                return this._footstepsAsQuadruped;
            }

            public FootprintStyle get_playerFootprintStyle() {
                return this._playerFootprintStyle;
            }
        }

        public static class Effects {

            private final BooleanValue showBreath;

            private boolean _showBreath;

            public Effects(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Defines mob effect generation")
                        .push("Mob Effect Options");

                this.showBreath = builder
                        .comment("Show breath effect")
                        .translation("mobeffects.cfg.effects.Breath")
                        .define("Show Breath Effect", true);

                builder.pop();
            }

            void update() {
                this._showBreath = this.showBreath.get();
            }

            public boolean get_showBreath() {
                return this._showBreath;
            }
        }
    }
}
