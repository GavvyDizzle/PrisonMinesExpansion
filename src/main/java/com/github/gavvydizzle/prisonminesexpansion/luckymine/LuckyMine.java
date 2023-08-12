package com.github.gavvydizzle.prisonminesexpansion.luckymine;

import com.github.gavvydizzle.prisonmines.mines.Mine;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.Pair;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.NoiseFilter;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.noise.RandomNoise;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Holds all the lucky mines for a given mine
 */
public class LuckyMine {

    private final LuckyMineManager luckyMineManager;
    private final double percentChance, percentRemainingThreshold;
    private final List<Pair<Integer, LuckyContents>> chances;
    private final Map<String, LuckyContents> contentsMap;
    private int totalWeight;
    private boolean ignored;

    public LuckyMine(LuckyMineManager luckyMineManager, double percentChance, double percentRemainingThreshold) {
        this.luckyMineManager = luckyMineManager;
        this.percentChance = percentChance;
        this.percentRemainingThreshold = percentRemainingThreshold;
        chances = new ArrayList<>();
        contentsMap = new HashMap<>();
        totalWeight = 0;
        ignored = false;
    }

    public void addLuckyContents(LuckyContents contents) {
        totalWeight += contents.weight();
        chances.add(new Pair<>(totalWeight, contents));
        contentsMap.put(contents.id(), contents);
    }

    /**
     * @return If this LuckyMine should randomly activate
     */
    public boolean shouldActivate() {
        if (ignored) return false;

        return Numbers.percentChance(percentChance);
    }

    /**
     * Handles this LuckyMine
     * @param mine The mine to edit
     */
    public void handleActivation(Mine mine) {
        LuckyContents luckyContents = getRandomLuckyContents();
        if (luckyContents == null || luckyContents.blocks().isEmpty()) return;

        com.sk89q.worldedit.world.World w = BukkitAdapter.adapt(mine.getWorld());
        CuboidRegion selection = mine.getRegion().clone();

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(w)) {
            RandomPattern pat = new RandomPattern();

            // Apply a mask to only convert some of the blocks in the region
            // This is the "Random Noise Mask" in the WorldEdit docs
            editSession.setMask(new NoiseFilter(new RandomNoise(), luckyContents.replaceBlockChance()));

            // Make the various WorldEdit block states by using the BukkitAdapter from the spigot block data
            for (Pair<Material, Double> pair : luckyContents.blocks()) {
                if (pair.second() > 0) {
                    pat.add(BukkitAdapter.adapt(pair.first().createBlockData()), pair.second());
                }
            }

            // Pass in the region and pattern
            editSession.setBlocks(selection, pat);

        } catch (MaxChangedBlocksException ex) {
            ex.printStackTrace();
            return;
        }

        if (!luckyContents.startMessages().isEmpty()) {
            luckyMineManager.sendStartMessage(luckyContents.startMessages(), luckyContents.hoverMessage());
        }
    }

    /**
     * Handles this LuckyMine
     * @param mine The mine to edit
     * @param luckyContents The contents to use
     */
    public void handleActivation(Mine mine, LuckyContents luckyContents) {
        if (luckyContents == null || luckyContents.blocks().isEmpty()) return;

        com.sk89q.worldedit.world.World w = BukkitAdapter.adapt(mine.getWorld());
        CuboidRegion selection = mine.getRegion().clone();

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(w)) {
            RandomPattern pat = new RandomPattern();

            // Apply a mask to only convert some of the blocks in the region
            // This is the "Random Noise Mask" in the WorldEdit docs
            editSession.setMask(new NoiseFilter(new RandomNoise(), luckyContents.replaceBlockChance()));

            // Make the various WorldEdit block states by using the BukkitAdapter from the spigot block data
            for (Pair<Material, Double> pair : luckyContents.blocks()) {
                if (pair.second() > 0) {
                    pat.add(BukkitAdapter.adapt(pair.first().createBlockData()), pair.second());
                }
            }

            // Pass in the region and pattern
            editSession.setBlocks(selection, pat);

        } catch (MaxChangedBlocksException ex) {
            ex.printStackTrace();
            return;
        }

        if (!luckyContents.startMessages().isEmpty()) {
            luckyMineManager.sendStartMessage(luckyContents.startMessages(), luckyContents.hoverMessage());
        }
    }

    /**
     * @return A random LuckyContents from this LuckyMine
     */
    @Nullable
    private LuckyContents getRandomLuckyContents() {
        if (chances.isEmpty() || totalWeight <= 0) return null;

        int random = Numbers.randomNumber(0, totalWeight - 1);

        for (Pair<Integer, LuckyContents> pair : chances) {
            if (random < pair.first()) {
                return pair.second();
            }
        }

        return chances.get(0).second();
    }

    public boolean isEmpty() {
        return chances.isEmpty();
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public double getPercentRemainingThreshold() {
        return percentRemainingThreshold;
    }

    public Collection<String> getContentIDs() {
        return contentsMap.keySet();
    }

    @Nullable
    public LuckyContents getLuckyContents(String id) {
        return contentsMap.get(id);
    }
}
