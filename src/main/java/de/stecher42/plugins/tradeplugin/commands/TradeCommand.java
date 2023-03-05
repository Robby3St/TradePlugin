package de.stecher42.plugins.tradeplugin.commands;

import de.stecher42.plugins.tradeplugin.main.Main;
import de.stecher42.plugins.tradeplugin.utils.DealMaker;
import de.stecher42.plugins.tradeplugin.utils.MessageStrings;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class TradeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {
            final String WRONG_USAGE = Main.PREFIX + "Wrong usage of the command /trade! Please use /trade <Name> or " +
                    "/trade accept, to accept an incoming trade.";

            Player p = (Player) sender;
            if(p.hasPermission("trade.trade")) {
                DealMaker dm = Main.getPlugin().getDealMaker();
                if(args.length == 1 && !args[0].replace(" ", "").equals("")) {
                    if(args[0].equalsIgnoreCase("accept")) {
                        dm.acceptTrade(p);
                        return true;
                    } else if(args[0].equalsIgnoreCase("cancel")) {
                        dm.cancelOwnTrade(p);
                    } else if(args[0].equalsIgnoreCase("deny")) {
                        dm.denyTrade(p);
                    } else if(Bukkit.getPlayer(args[0]) != null) {
                        Player opposite = Bukkit.getPlayer(args[0]);
                        boolean success = dm.makeTradeOffer(p, opposite);
                        if(success)
                            p.sendMessage(String.format("%sThe trade request was now send to %s!", Main.PREFIX, args[0]));
                    } else {
                        p.sendMessage(String.format("%sCould not find a player with the name '%s'. Please use " +
                                "/trade <Name> or /trade accept, to accept an incoming trade!", Main.PREFIX, args[0]));
                    }
                } else if(args.length == 2) {
                    if(args[0].equalsIgnoreCase("accept")) {
                        if(Bukkit.getPlayer(args[0]) != null) {
                            dm.acceptTrade(p, Objects.requireNonNull(Bukkit.getPlayer(args[0])));
                            return true;
                        } else {
                            p.sendMessage(String.format("%sCould not find a player with the name '%s'. Please use " +
                                    "/trade <Name> or /trade accept, to accept an incoming trade!", Main.PREFIX, args[0]));
                        }
                    } else if(args[0].equalsIgnoreCase("deny")) {
                        if(Bukkit.getPlayer(args[1]) != null) {
                            dm.denyTrade(p, Objects.requireNonNull(Bukkit.getPlayer(args[1])));
                        } else {
                            p.sendMessage(Main.PREFIX + "Could not find a player with that name!");
                        }
                    }
                } else {
                    p.sendMessage(WRONG_USAGE);
                }
            } else {
                p.sendMessage(MessageStrings.NO_PERMISSION);
            }
        } else {
            sender.sendMessage("You must be a player, to do this!");
        }
        return true;
    }
}