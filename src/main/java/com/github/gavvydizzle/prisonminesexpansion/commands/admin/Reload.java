package com.github.gavvydizzle.prisonminesexpansion.commands.admin;

import com.github.gavvydizzle.prisonminesexpansion.PrisonMinesExpansion;
import com.github.gavvydizzle.prisonminesexpansion.commands.AdminCommandManager;
import com.github.gavvydizzle.prisonminesexpansion.luckymine.LuckyMineManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Reload extends SubCommand {

    private final List<String> subReloadList = List.of("luckyMine");

    private final LuckyMineManager luckyMineManager;

    public Reload(AdminCommandManager commandManager, LuckyMineManager luckyMineManager) {
        this.luckyMineManager = luckyMineManager;

        setName("reload");
        setDescription("Reloads this plugin or a specified part");
        setSyntax("/" + commandManager.getCommandDisplayName() + " reload [arg]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("luckyMine")) {
                reloadLuckyMine();
                sender.sendMessage(ChatColor.GREEN + "[PrisonMinesExpansion] Reloaded LuckyMine");
            }
            else {
                sender.sendMessage(ChatColor.RED + "Invalid sub-argument. Nothing was reloaded");
            }
        }
        else {
            PrisonMinesExpansion.getInstance().reloadConfig();

            reloadLuckyMine();

            sender.sendMessage(ChatColor.GREEN + "[PrisonMinesExpansion] Reloaded");
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], subReloadList, list);
        }
        return list;
    }

    private void reloadLuckyMine() {
        PrisonMinesExpansion.getConfigManager().reload("luckyMine");
        luckyMineManager.reload();
        PrisonMinesExpansion.getConfigManager().save("luckyMine");
    }
}