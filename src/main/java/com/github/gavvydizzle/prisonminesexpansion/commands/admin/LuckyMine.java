package com.github.gavvydizzle.prisonminesexpansion.commands.admin;

import com.github.gavvydizzle.prisonminesexpansion.commands.AdminCommandManager;
import com.github.gavvydizzle.prisonminesexpansion.luckymine.LuckyMineManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class LuckyMine extends SubCommand {

    private final List<String> args2 = List.of("info", "start");

    private final LuckyMineManager luckyMineManager;

    public LuckyMine(AdminCommandManager commandManager, LuckyMineManager luckyMineManager) {
        this.luckyMineManager = luckyMineManager;

        setName("luckymine");
        setDescription("LuckyMine commands");
        setSyntax("/" + commandManager.getCommandDisplayName() + " luckymine [arg] ...");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        if (args[1].equalsIgnoreCase("info")) {
            if (args.length == 2) {
                luckyMineManager.sendInfoMessage(sender);
            }
            else {
                luckyMineManager.sendInfoMessage(sender, args[2]);
            }
        }
        else if (args[1].equalsIgnoreCase("start")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "You must specify a mine");
                return;
            }

            String mineID = args[2];
            if (args.length == 3) {
                if (luckyMineManager.forceLuckyMine(mineID)) {
                    sender.sendMessage(ChatColor.GREEN + "Successfully started a lucky mine in mine: " + mineID);
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Unable to start lucky mine");
                }
            }
            else {
                String contentsID = args[3];
                if (luckyMineManager.forceLuckyMine(mineID, contentsID)) {
                    sender.sendMessage(ChatColor.GREEN + "Successfully started a lucky mine in mine " + mineID + " with contents list " + contentsID);
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Unable to start lucky mine");
                }
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "Invalid sub-argument");
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], args2, list);
        }
        else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], luckyMineManager.getLuckyMineIDs(), list);
        }
        else if (args.length == 4) {
            if (args[1].equalsIgnoreCase("start")) {
                StringUtil.copyPartialMatches(args[3], luckyMineManager.getLuckyMineContentsIDs(args[2]), list);
            }
        }
        return list;
    }
}