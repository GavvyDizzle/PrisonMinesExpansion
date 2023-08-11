package com.github.gavvydizzle.prisonminesexpansion.luckymine;

import com.github.mittenmc.serverutils.Pair;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Holds the properties of a single LuckyMine reset
 */
public record LuckyContents(String id, int weight, double replaceBlockChance, List<Pair<Material, Double>> blocks, List<String> startMessages, @Nullable HoverEvent hoverMessage) {}
