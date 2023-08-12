package com.github.gavvydizzle.prisonminesexpansion.luckymine;

import com.github.gavvydizzle.prisonmines.api.PrisonMinesAPI;
import com.github.gavvydizzle.prisonmines.events.MinePostResetEvent;
import com.github.gavvydizzle.prisonmines.events.MinesReloadedEvent;
import com.github.gavvydizzle.prisonmines.mines.Mine;
import com.github.gavvydizzle.prisonminesexpansion.PrisonMinesExpansion;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.Pair;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Handles all LuckyMines
 */
public class LuckyMineManager implements Listener {

    private final PrisonMinesExpansion instance;
    private final PrisonMinesAPI prisonMinesAPI;
    private final Map<String, LuckyMine> luckyMineMap;

    public LuckyMineManager(PrisonMinesExpansion instance, PrisonMinesAPI prisonMinesAPI) {
        this.instance = instance;
        this.prisonMinesAPI = prisonMinesAPI;
        luckyMineMap = new HashMap<>();

        reload();
    }

    public void reload() {
        FileConfiguration config = PrisonMinesExpansion.getConfigManager().get("luckyMine");
        if (config == null) return;

        config.addDefault("list", new HashMap<>());

        luckyMineMap.clear();

        if (config.getConfigurationSection("list") != null) {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("list")).getKeys(false)) {
                String path = "list." + key;

                String mineID = config.getString(path + ".mineID");
                if (mineID == null) continue;
                mineID = mineID.toLowerCase();

                if (prisonMinesAPI.getMineByID(mineID) == null) {
                    instance.getLogger().warning("No mine exists for the id '" + mineID + "'. Ignoring lucky mine at " + path);
                    continue;
                }

                if (luckyMineMap.containsKey(mineID)) {
                    instance.getLogger().warning("A lucky mine already exists for mine '" + mineID + "'. Ignoring duplicate lucky mine at " + path);
                    continue;
                }

                double percentChancePerReset = config.getDouble(path + ".percentChancePerReset");
                if (percentChancePerReset <= 0) {
                    instance.getLogger().warning("Non-positive percentChancePerReset '" + percentChancePerReset + "'. Ignoring lucky mine at " + path);
                    continue;
                }

                // No error checking, just clamp this amount between 0 and 100
                double percentRemainingThreshold = Numbers.constrain(config.getDouble(path + ".percentRemainingThreshold"), 0, 100);

                LuckyMine luckyMine = new LuckyMine(this, percentChancePerReset, percentRemainingThreshold);

                if (config.getConfigurationSection(path + ".contents") == null) {
                    instance.getLogger().warning("No contents section exists for lucky mine at " + path + ". It will not be loaded");
                    continue;
                }

                for (String key2 : Objects.requireNonNull(config.getConfigurationSection(path + ".contents")).getKeys(false)) {
                    String contentsPath = path + ".contents." + key2;

                    int weight = config.getInt(contentsPath + ".weight");
                    if (weight <= 0) {
                        instance.getLogger().warning("Non-positive weight '" + weight + "'. Ignoring lucky mine contents at " + contentsPath);
                        continue;
                    }

                    double replaceBlockChance = config.getDouble(contentsPath + ".replaceBlockChance");
                    if (replaceBlockChance <= 0 || replaceBlockChance > 1) {
                        instance.getLogger().warning("Invalid replaceBlockChance '" + replaceBlockChance + "'. It must be (0,1]. Ignoring lucky mine contents at " + contentsPath);
                        continue;
                    }

                    List<Pair<Material, Double>> pairs = parseBlockList(config.getStringList(contentsPath + ".blocks"), contentsPath + ".blocks");
                    if (pairs.isEmpty()) {
                        instance.getLogger().warning("No blocks loaded. Ignoring lucky mine contents at " + contentsPath);
                        continue;
                    }

                    luckyMine.addLuckyContents(new LuckyContents(
                            key2.toLowerCase(),
                            weight,
                            replaceBlockChance,
                            pairs,
                            Colors.conv(config.getStringList(contentsPath + ".messages")),
                            generateHoverComponent(config.getStringList(contentsPath + ".hoverMessage"))
                    ));
                }

                if (luckyMine.isEmpty()) {
                    instance.getLogger().warning("No contents loaded for mine '" + mineID + "'. Ignoring lucky mine at " + path);
                    continue;
                }

                // Finally add the new LuckyMine to the map
                luckyMineMap.put(mineID, luckyMine);
            }
        }
    }

    /**
     * Parses a string list in the form MATERIAL:weight
     * @param list The list of strings to parse
     * @param path The config path for error logging
     * @return The converted list of entries
     */
    private List<Pair<Material, Double>> parseBlockList(List<String> list, String path) {
        List<Pair<Material, Double>> pairs = new ArrayList<>();

        for (String str : list) {
            String[] arr = str.split(":");

            if (arr.length != 2) {
                instance.getLogger().warning("Invalid syntax '" + str + "' at " + path);
                continue;
            }

            Material material = Material.getMaterial(arr[0].toUpperCase());
            if (material == null) {
                instance.getLogger().warning("Unknown material '" + arr[0] + "' for '" + str + "' at " + path);
                continue;
            }

            if (containsMaterial(pairs, material)) {
                instance.getLogger().warning("Material '" + arr[0] + "' defined multiple times. Ignoring '" + str + "' at " + path);
                continue;
            }

            double weight;
            try {
                weight = Double.parseDouble(arr[1]);
            } catch (Exception ignored) {
                instance.getLogger().warning("Invalid weight '" + arr[1] + "' for '" + str + "' at " + path);
                continue;
            }

            if (weight <= 0) {
                instance.getLogger().warning("Non-positive weight '" + arr[1] + "'. Ignoring '" + str + "' at " + path);
                continue;
            }

            pairs.add(new Pair<>(material, weight));
        }

        return pairs;
    }

    private boolean containsMaterial(List<Pair<Material, Double>> list, Material material) {
        for (Pair<Material, Double> pair : list) {
            if (pair.first() == material) return true;
        }
        return false;
    }

    @Nullable
    private HoverEvent generateHoverComponent(@Nullable List<String> list) {
        if (list == null || list.isEmpty()) return null;

        ComponentBuilder componentBuilder = new ComponentBuilder();
        for (int i = 0; i < list.size() - 1; i++) {
            componentBuilder.append(Colors.conv(list.get(i) + "\n"));
        }
        componentBuilder.append(Colors.conv(list.get(list.size() - 1)));

        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(componentBuilder.create()));
    }

    @EventHandler
    private void onMineReset(MinePostResetEvent e) {
        LuckyMine luckyMine = luckyMineMap.get(e.getMine().getId());
        if (luckyMine == null) return;

        // If the mine has not had enough blocks broken to initiate a lucky mine attempt
        // If the mine is ignored, then this check is skipped because a lucky mine is being forced to start
        if (!luckyMine.isIgnored() && e.getPercentRemaining() > luckyMine.getPercentRemainingThreshold()) return;

        // Randomly start a lucky mine
        if (luckyMine.shouldActivate()) luckyMine.handleActivation(e.getMine());

        // The mine is only ever ignored when force reset. This will cause it to be ignored for exactly one reset
        luckyMine.setIgnored(false);
    }

    @EventHandler
    private void onMineReload(MinesReloadedEvent e) {
        instance.getLogger().info("PrisonMines reload detected. Now reloading LuckyMines");
        reload();
    }

    /**
     * Sends the messages to all online players
     * @param messages The messages to send
     * @param hoverEvent The hover event to add if not null
     */
    public void sendStartMessage(List<String> messages, @Nullable HoverEvent hoverEvent) {
        String[] arr = messages.toArray(new String[0]);

        if (hoverEvent == null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(arr);
            }
        }
        else {
            TextComponent[] arr2 = new TextComponent[arr.length];

            for (int i = 0; i < arr.length - 1; i++) {
                arr2[i] = new TextComponent(arr[i] + "\n");
                arr2[i].setHoverEvent(hoverEvent);
            }
            arr2[arr.length - 1] = new TextComponent(arr[arr.length - 1]);
            arr2[arr.length - 1].setHoverEvent(hoverEvent);

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(arr2);
            }
        }
    }

    public void sendInfoMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_GREEN + "[LuckyMine]" + ChatColor.YELLOW + " There are " + luckyMineMap.size() + " lucky mines active");
    }

    public void sendInfoMessage(CommandSender sender, String mineID) {
        LuckyMine luckyMine = luckyMineMap.get(mineID);
        if (luckyMine == null) {
            sender.sendMessage(ChatColor.RED + "Invalid mine ID");
            return;
        }

        sender.sendMessage(ChatColor.DARK_GREEN + "[LuckyMine]" + ChatColor.YELLOW + " Mine '" + mineID + "' has " + luckyMine.getContentIDs().size() + " contents loaded");
        sender.sendMessage(ChatColor.DARK_GREEN + "[LuckyMine]" + ChatColor.WHITE + " " + luckyMine.getContentIDs());
    }

    /**
     * Forces a lucky mine in this mine
     * @param mineID The ID of the mine
     * @return If the lucky mine started successfully
     */
    public boolean forceLuckyMine(String mineID) {
        LuckyMine luckyMine = luckyMineMap.get(mineID);
        if (luckyMine == null) return false;

        Mine mine = prisonMinesAPI.getMineByID(mineID);
        if (mine == null) return false;

        // Must set the LuckyMine to ignore because it could double-activate otherwise
        luckyMine.setIgnored(true);
        mine.resetMine(false);
        luckyMine.handleActivation(mine);

        return true;
    }

    /**
     * Forces a lucky mine in this mine
     * @param mineID The ID of the mine
     * @param contentsID Which set of contents to use
     * @return If the lucky mine started successfully
     */
    public boolean forceLuckyMine(String mineID, String contentsID) {
        LuckyMine luckyMine = luckyMineMap.get(mineID);
        if (luckyMine == null) return false;

        Mine mine = prisonMinesAPI.getMineByID(mineID);
        if (mine == null) return false;

        LuckyContents luckyContents = luckyMine.getLuckyContents(contentsID);
        if (luckyContents == null) return false;

        // Must set the LuckyMine to ignore because it could double-activate otherwise
        luckyMine.setIgnored(true);
        mine.resetMine(false);
        luckyMine.handleActivation(mine, luckyContents);

        return true;
    }

    public Collection<String> getLuckyMineIDs() {
        return luckyMineMap.keySet();
    }

    public Collection<String> getLuckyMineContentsIDs(String mineID) {
        LuckyMine luckyMine = luckyMineMap.get(mineID);
        if (luckyMine == null) return Collections.emptyList();

        return luckyMine.getContentIDs();
    }
}
