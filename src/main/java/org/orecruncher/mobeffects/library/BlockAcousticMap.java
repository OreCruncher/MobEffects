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

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.blockstate.BlockStateMatcher;
import org.orecruncher.lib.collections.ObjectArray;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.Block;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;

@OnlyIn(Dist.CLIENT)
public final class BlockAcousticMap {

	//protected final Map<BlockState, IAcoustic> cache = new Reference2ObjectOpenHashMap<>();
	protected final Map<Block, ObjectArray<BlockMapEntry>> data = new Reference2ObjectOpenHashMap<>();
	protected final Function<BlockState, IAcoustic> resolver;

	public BlockAcousticMap() {
		this(s -> null);
	}

	public BlockAcousticMap(@Nonnull final Function<BlockState, IAcoustic> resolver) {
		this.resolver = resolver;
		put(BlockStateMatcher.create(Blocks.AIR.getDefaultState()), Constants.NOT_EMITTER);
		put(BlockStateMatcher.create(Blocks.CAVE_AIR.getDefaultState()), Constants.NOT_EMITTER);
		put(BlockStateMatcher.create(Blocks.VOID_AIR.getDefaultState()), Constants.NOT_EMITTER);
	}

	@Nonnull
	protected IAcoustic cacheMiss(@Nonnull final BlockState state) {
		IAcoustic result = null;
		final ObjectArray<BlockMapEntry> entries = this.data.get(state.getBlock());
		if (entries != null) {
			final BlockStateMatcher matcher = BlockStateMatcher.create(state);
			result = find(entries, matcher);
			if (result != null)
				return result;
			result = find(entries, BlockStateMatcher.asGeneric(state));
			if (result != null)
				return result;
		}

		return Utilities.firstNonNull(this.resolver.apply(state), Constants.EMPTY);
	}

	@Nullable
	private IAcoustic find(@Nonnull final ObjectArray<BlockMapEntry> entries,
			@Nonnull final BlockStateMatcher matcher) {
		// Search backwards. In general highly specified states are at
		// the end of the array.
		for (int i = entries.size() - 1; i >= 0; i--) {
			final BlockMapEntry e = entries.get(i);
			if (matcher.equals(e.matcher))
				return e.acoustics;
		}
		return null;
	}

	/**
	 * Obtain acoustic information for a block. If the block has variants (subtypes)
	 * it will fall back to searching for a generic if a specific one is not found.
	 */
	@Nonnull
	public IAcoustic getBlockAcoustics(@Nonnull final BlockState state) {
		return cacheMiss(state);
		/*
		IAcoustic result = this.cache.get(state);
		if (result == null) {
			result = cacheMiss(state);
			this.cache.put(state, result);
		}
		return result;
		 */
	}

	public void put(@Nonnull final BlockStateMatcher info, @Nonnull final IAcoustic acoustics) {
		ObjectArray<BlockMapEntry> entry = this.data.get(info.getBlock());
		if (entry == null) {
			this.data.put(info.getBlock(), entry = new ObjectArray<>(2));
		}
		entry.add(new BlockMapEntry(info, acoustics));
	}

	public void clear() {
		//this.cache.clear();
		this.data.clear();
	}

	private static class BlockMapEntry {
		public final BlockStateMatcher matcher;
		public final IAcoustic acoustics;

		public BlockMapEntry(@Nonnull final BlockStateMatcher matcher, @Nonnull final IAcoustic acoustics) {
			this.matcher = matcher;
			this.acoustics = acoustics;
		}
	}
}