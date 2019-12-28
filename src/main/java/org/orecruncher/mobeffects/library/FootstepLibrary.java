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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.lib.JsonUtils;
import org.orecruncher.lib.TagUtils;
import org.orecruncher.lib.blockstate.BlockStateMatcher;
import org.orecruncher.lib.blockstate.BlockStateParser;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.mobeffects.Config;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.footsteps.*;
import org.orecruncher.mobeffects.library.config.ModConfig;
import org.orecruncher.mobeffects.library.config.VariatorConfig;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;
import org.orecruncher.sndctrl.events.BlockInspectionEvent;
import org.orecruncher.sndctrl.library.AcousticLibrary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = MobEffects.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FootstepLibrary {

    private static IModLog LOGGER = MobEffects.LOGGER.createChild(FootstepLibrary.class);

    private static final String TEXT_FOOTSTEPS = TextFormatting.DARK_PURPLE + "<Footsteps>";
    private static final BlockAcousticMap metaMap = new BlockAcousticMap();
    private static final Map<Substrate, BlockAcousticMap> substrateMap = new EnumMap<>(Substrate.class);

    private static final List<String> FOOTPRINT_SOUND_PROFILE =
            Arrays.asList(
                    "minecraft:block.sand.step",
                    "minecraft:block.gravel.step",
                    "minecraft:block.snow.step"
            );

    private static final Set<Material> FOOTPRINT_MATERIAL = new ReferenceOpenHashSet<>();
    private static final Set<BlockState> FOOTPRINT_STATES = new ReferenceOpenHashSet<>();
    private static final Map<String, List<MacroEntry>> macros = new Object2ObjectOpenHashMap<>();
    private static final Map<String, Variator> variators = new Object2ObjectOpenHashMap<>();

    private static Variator defaultVariator;
    private static Variator childVariator;
    private static Variator playerVariator;
    private static Variator playerQuadrupedVariator;

    static {

        // Initialize the known materials that leave footprints
        FOOTPRINT_MATERIAL.add(Material.CLAY);
        FOOTPRINT_MATERIAL.add(Material.EARTH);
        FOOTPRINT_MATERIAL.add(Material.SPONGE);
        FOOTPRINT_MATERIAL.add(Material.ICE);
        FOOTPRINT_MATERIAL.add(Material.PACKED_ICE);
        FOOTPRINT_MATERIAL.add(Material.SAND);
        FOOTPRINT_MATERIAL.add(Material.SNOW_BLOCK);
        FOOTPRINT_MATERIAL.add(Material.SNOW);
        FOOTPRINT_MATERIAL.add(Material.CAKE);

        final MacroEntry MESSY = new MacroEntry("messy", "messy_ground");
        final MacroEntry NOT_EMITTER = new MacroEntry(null, "not_emitter");

        List<MacroEntry> entries = new ArrayList<>();
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "straw"));
        macros.put("#sapling", entries);
        macros.put("#reed", entries);

        entries = new ArrayList<>();
        entries.add(new MacroEntry(null, "leaves"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "brush"));
        macros.put("#plant", entries);

        entries = new ArrayList<>();
        entries.add(new MacroEntry(null, "leaves"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "brush_straw_transition"));
        macros.put("#bush", entries);

        entries = new ArrayList<>();
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("bigger", "bluntwood"));
        macros.put("#fence", entries);

        entries = new ArrayList<>();
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("foliage", "rails"));
        macros.put("#rail", entries);

        entries = new ArrayList<>();
        entries.add(new MacroEntry(null, "straw"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "straw"));
        macros.put("#vine", entries);

        entries = new ArrayList<>();
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("carpet", "rug"));
        macros.put("#moss", entries);

        entries = new ArrayList<>();
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("age", "0", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "1", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "2", "foliage", "brush"));
        entries.add(new MacroEntry("age", "3", "foliage", "brush"));
        entries.add(new MacroEntry("age", "4", "foliage", "brush_straw_transition"));
        entries.add(new MacroEntry("age", "5", "foliage", "brush_straw_transition"));
        entries.add(new MacroEntry("age", "6", "foliage", "straw"));
        entries.add(new MacroEntry("age", "7", "foliage", "straw"));
        macros.put("#wheat", entries);

        entries = new ArrayList<>();
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("age", "0", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "1", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "2", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "3", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "4", "foliage", "brush"));
        entries.add(new MacroEntry("age", "5", "foliage", "brush"));
        entries.add(new MacroEntry("age", "6", "foliage", "brush"));
        entries.add(new MacroEntry("age", "7", "foliage", "brush"));
        macros.put("#crop", entries);

        entries = new ArrayList<>();
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("age", "0", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "1", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "2", "foliage", "brush"));
        entries.add(new MacroEntry("age", "3", "foliage", "brush"));
        macros.put("#beets", entries);
    }

    private FootstepLibrary() {

    }

    public static void initialize() {

        // Load up the variators
        final Map<String, VariatorConfig> variatorMap = JsonUtils.loadConfig(new ResourceLocation(MobEffects.MOD_ID, "variators.json"), VariatorConfig.class);
        for (final Map.Entry<String, VariatorConfig> kvp : variatorMap.entrySet()) {
            variators.put(kvp.getKey(), new Variator(kvp.getValue()));
        }

    	defaultVariator = getVariator("default");
    	childVariator = getVariator("child");
		playerVariator = getVariator(Config.CLIENT.footsteps.firstPersonFootstepCadence.get() ? "player_slow" : "player");
		playerQuadrupedVariator = getVariator(Config.CLIENT.footsteps.firstPersonFootstepCadence.get() ? "quadruped_slow" : "quadruped");

        if (Config.CLIENT.logging.enableLogging.get()) {
            LOGGER.info("Registered Variators");
            LOGGER.info("====================");

            for (final String v : variators.keySet()) {
                LOGGER.info(v);
            }
        }

        // Load up footstep info
        try {

            final ModConfig mod = JsonUtils.load(new ResourceLocation(MobEffects.MOD_ID, "data/mcp.json"), ModConfig.class);

            // Handle our primitives first
            for (final Map.Entry<String, String> kvp : mod.primitives.entrySet()) {
                final ResourceLocation loc = AcousticLibrary.resolveResource(MobEffects.MOD_ID, kvp.getKey());
                AcousticLibrary.resolve(loc, kvp.getValue());
            }

            // Apply acoustics based on configured tagging
            for (final Map.Entry<String, String> kvp : mod.tagged.entrySet()) {
                registerTag(kvp.getKey(), kvp.getValue());
            }

            // Now do the regular block stuff
            for (final Map.Entry<String, String> kvp : mod.footsteps.entrySet()) {
                register(kvp.getKey(), kvp.getValue());
            }

        } catch(@Nonnull final Throwable t) {
            LOGGER.error(t, "Unable to load MCP config data!");
        }
	}

    private static void seedMap() {
        // Iterate through the blockmap looking for known pattern types.
        // Though they probably should all be registered with Forge
        // dictionary it's not a requirement.
        for (final Block block : ForgeRegistries.BLOCKS) {
            final String blockName = Objects.requireNonNull(block.getRegistryName()).toString();
            if (block instanceof CropsBlock) {
                final CropsBlock crop = (CropsBlock) block;
                if (crop.getMaxAge() == 3) {
                    // Like beets
                    registerBlocks("#beets", blockName);
                } else if (blockName.equals("minecraft:wheat")) {
                    // Wheat is special because it is straw like
                    registerBlocks("#wheat", blockName);
                } else if (crop.getMaxAge() == 7) {
                    // Like carrots and potatoes
                    registerBlocks("#crop", blockName);
                }
            } else if (block instanceof SaplingBlock) {
                registerBlocks("#sapling", blockName);
            } else if (block instanceof SugarCaneBlock) {
                registerBlocks("#reed", blockName);
            } else if (block instanceof FenceBlock) {
                registerBlocks("#fence", blockName);
            } else if (block instanceof VineBlock) {
                registerBlocks("#vine", blockName);
            } else if (block instanceof FlowerBlock || block instanceof MushroomBlock) {
                registerBlocks("not_emitter", blockName);
            } else if (block instanceof LogBlock) {
                registerBlocks("log", blockName);
            } else if (block instanceof DoorBlock) {
                registerBlocks("bluntwood", blockName);
            } else if (block instanceof LeavesBlock) {
                registerBlocks("leaves", blockName);
            } else if (block instanceof OreBlock) {
                registerBlocks("ore", blockName);
            } else if (block instanceof IceBlock) {
                registerBlocks("ice", blockName);
            } else if (block instanceof ChestBlock) {
                registerBlocks("squeakywood", blockName);
            } else if (block instanceof GlassBlock) {
                registerBlocks("glass", blockName);
            } else if (block instanceof BedBlock) {
                registerBlocks("rug", blockName);
            } else if (block instanceof AbstractRailBlock) {
                registerBlocks("#rail", blockName);
            } else {
                // Register generics based on sound type
                final BlockState state = block.getDefaultState();
                if (state.getMaterial() != Material.AIR) {
                    final SoundType st = state.getSoundType();
                    registerBlocks(st.getStepSound().getName().toString(), blockName);
                }
            }
        }
    }

    private static void registerBlocks(@Nonnull final String blockClass, @Nonnull final String... blocks) {
        for (final String s : blocks)
            register(s, blockClass);
    }

    @SubscribeEvent
	public static void onInspectionEvent(@Nonnull final BlockInspectionEvent evt) {
        evt.data.add(TEXT_FOOTSTEPS);
        collectData(evt.state, evt.data);
    }

    public static boolean hasAcoustics(@Nonnull final BlockState state) {
        return metaMap.getBlockAcoustics(state) != Constants.EMPTY;
    }

	@Nonnull
	private static Variator getVariator(@Nonnull final String varName) {
		return variators.getOrDefault(varName, defaultVariator);
	}

	@Nonnull
    public static IAcoustic getBlockAcoustics(@Nonnull final BlockState state) {
        return getBlockAcoustics(state, null);
    }

    @Nonnull
    public static IAcoustic getBlockAcoustics(@Nonnull final BlockState state, @Nullable final Substrate substrate) {
        // Walking an edge of a block can produce this
        if (state == Blocks.AIR.getDefaultState())
            return Constants.NOT_EMITTER;
        if (substrate != null) {
            final BlockAcousticMap sub = substrateMap.get(substrate);
            return sub != null ? sub.getBlockAcoustics(state) : Constants.EMPTY;
        }
        return metaMap.getBlockAcoustics(state);
    }

    private static void put(@Nonnull final BlockStateMatcher info, @Nullable final String substrate,
                            @Nonnull final String acousticList) {

        final Substrate s = Substrate.get(substrate);
        final IAcoustic acoustics = AcousticLibrary.resolve(
                MobEffects.MOD_ID,
                acousticList,
                r -> {
                    if (r.getPath().equals("not_emitter"))
                        return Constants.NOT_EMITTER;
                    if (r.getPath().equals("messy_ground"))
                        return Constants.MESSY_GROUND;
                    return null;
                });

        if (s == null) {
            metaMap.put(info, acoustics);
        } else {
            BlockAcousticMap sub = substrateMap.get(s);
            if (sub == null)
                substrateMap.put(s, sub = new BlockAcousticMap());
            sub.put(info, acoustics);
        }
    }

    private static void register0(@Nonnull final String key, @Nonnull final String acousticList) {

        final Optional<BlockStateParser.ParseResult> parseResult = BlockStateParser.parseBlockState(key);
        if (parseResult.isPresent()) {
            final BlockStateParser.ParseResult name = parseResult.get();
            final BlockStateMatcher matcher = BlockStateMatcher.create(name);
            if (matcher.isEmpty()) {
                LOGGER.warn("Unable to identify block state '%s'", key);
            } else {
                final String substrate = name.getExtras();
                put(matcher, substrate, acousticList);
            }
        } else {
            LOGGER.warn("Malformed key in blockMap '%s'", key);
        }
    }

    private static void registerTag(@Nonnull String tagName, @Nonnull final String acousticList) {
        String substrate = null;
        final int idx = tagName.indexOf('+');

        if (idx >= 0) {
            substrate = tagName.substring(idx + 1);
            tagName = tagName.substring(0, idx);
        }

        final Tag<Block> blockTag = TagUtils.getBlockTag(tagName);
        if (blockTag != null) {
            for (final Block b : blockTag.getAllElements()) {
                String blockName = Objects.requireNonNull(b.getRegistryName()).toString();
                if (substrate != null)
                    blockName = blockName + "+" + substrate;
                register(blockName, acousticList);
            }
        } else {
            LOGGER.warn("Unable to identify block tag '%s'", tagName);
        }
    }

    private static void register(@Nonnull final String key, @Nonnull final String acousticList) {
        if (acousticList.startsWith("#")) {
            final List<MacroEntry> macro = macros.get(acousticList);
            if (macro != null) {
                macro.stream()
                        .map(m -> m.expand(key))
                        .forEach(t -> register0(t.getA(), t.getB()));
            } else {
                LOGGER.debug("Unknown macro '%s'", acousticList);
            }
        } else {
            register0(key, acousticList);
        }
    }

    @Nonnull
    public static Generator createGenerator(@Nonnull final LivingEntity entity) {
        Variator var;
        if (entity.isChild()) {
            var = childVariator;
        } else if (entity instanceof PlayerEntity) {
            var = Config.CLIENT.footsteps.footstepsAsQuadruped.get() ? playerQuadrupedVariator : playerVariator;
        } else {
            var = getVariator(entity.getType().getRegistryName().toString());
        }

        return var.QUADRUPED ? new GeneratorQP(var) : new Generator(var);
    }

    private static void collectData(@Nonnull final BlockState state, @Nonnull final List<String> data) {

        final int s = data.size();
        final IAcoustic temp = getBlockAcoustics(state);
        if (temp != Constants.EMPTY)
            data.add(temp.toString());

        for (final Map.Entry<Substrate, BlockAcousticMap> e : substrateMap.entrySet()) {
            final IAcoustic acoustics = e.getValue().getBlockAcoustics(state);
            if (acoustics != Constants.EMPTY)
                data.add(e.getKey() + ":" + acoustics.toString());
        }

        if (s == data.size()) {
            data.add("** NONE **");
        }
    }

    public static boolean hasFootprint(@Nonnull final BlockState state) {
        return FOOTPRINT_MATERIAL.contains(state.getMaterial()) || FOOTPRINT_STATES.contains(state);
    }

    private static class MacroEntry {
        public final String propertyName;
        public final String propertyValue;
        public final String substrate;
        public final String value;

        public MacroEntry(@Nullable final String substrate, @Nonnull final String value) {
            this(null, null, substrate, value);
        }

        public MacroEntry(@Nullable final String propertyName, @Nullable final String propertyValue,
                          @Nullable final String substrate, @Nonnull final String value) {
            this.propertyName = propertyName;
            this.propertyValue = propertyValue;
            this.substrate = substrate;
            this.value = value;
        }

        @Nonnull
        public Tuple<String, String> expand(@Nonnull final String base) {
            final StringBuilder builder = new StringBuilder();
            builder.append(base);
            if (this.propertyName != null) {
                builder.append('[');
                builder.append(this.propertyName).append('=').append(this.propertyValue);
                builder.append(']');
            }

            if (this.substrate != null) {
                builder.append('+').append(this.substrate);
            }

            return new Tuple<>(builder.toString(), this.value);
        }
    }

}
