package de.stecher42.plugins.tradeplugin.utils;

import de.stecher42.plugins.tradeplugin.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class DealMaker {
    private HashMap<UUID, Player> pairs = new HashMap<UUID, Player>(); // Owner saved as UUID in key
    private ArrayList<TradingWindow> currentDealInvs = new ArrayList<TradingWindow>();

    public boolean makeTradeOffer(Player owner, Player target) {
        if(owner.getUniqueId().equals(target.getUniqueId())) {
            owner.sendMessage(Main.PREFIX + "§cYou can't trade with yourself!");
            return false;
        } else if(pairs.containsKey(owner.getUniqueId())) {
            owner.sendMessage(Main.PREFIX + "§cYou already sent a trade request to §6" +
                    pairs.get(owner.getUniqueId()).getName() +
                    "§c! Please cancel the trade, by using §8/trade cancel§c first,");
            return false;
        } else {
            pairs.put(owner.getUniqueId(), target);
            target.sendMessage(Main.PREFIX + "You got a new trade offer by §6" + owner.getName() +
                    "§r! Use §8/trade accept <Name>§r, to deal.");
            target.playSound(target.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_3, 1.0f, 1.0f);
            owner.playSound(owner.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
            Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    Player opponent = Main.getPlugin().getDealMaker().cancelTrade(owner);
                    if(opponent != null) {
                        opponent.sendMessage("The trade request by §6" + owner.getName() + "§r expired!");
                        opponent.playSound(opponent.getLocation(), Sound.ENTITY_RAVAGER_CELEBRATE, 1.0f, 1.0f);
                        owner.sendMessage("Your trade request to §6" + opponent.getName() + "§r expired!");
                        owner.playSound(owner.getLocation(), Sound.ENTITY_WARDEN_HURT, 1.0f, 1.0f);
                    }
                }
            }, 20L * 60 * Main.getPlugin().getConfigValues().TIME_TRADING_REQUEST_SURVIVES);
            return true;
        }
    }

    public void acceptTrade(Player targeted, Player acceptedPlayer) {
        if(pairs.containsKey(acceptedPlayer.getUniqueId()) && pairs.get(acceptedPlayer.getUniqueId()).equals(targeted)) {
            TradingWindow trade = new TradingWindow(acceptedPlayer, targeted);
            pairs.remove(acceptedPlayer.getUniqueId());
        } else {
            targeted.sendMessage(Main.PREFIX + "This player is not in a trade offer with you. Sorry.");
        }
    }

    public void acceptTrade(Player targetted) {
        if(pairs.containsValue(targetted)) {
            boolean found = false;
            for(Player v : pairs.values()) {
                if(v.equals(targetted)) {
                    if(!found)
                        found = true;
                    else {
                        targetted.sendMessage(Main.PREFIX + "You got more than 1 trade offer! " +
                                "Please use /trade accept <Name> to accept a specific trade by a player");
                        return;
                    }
                }
            }
            for(UUID t : pairs.keySet()) {
                if(pairs.get(t).equals(targetted)) {
                    // Found trade offer pair
                    if(Bukkit.getPlayer(t) != null) { // Checking if trading partner is online
                        TradingWindow trade = new TradingWindow(Objects.requireNonNull(Bukkit.getPlayer(t)), targetted);
                    } else {
                        targetted.sendMessage(Main.PREFIX + "Sorry, but this player went offline!");
                    }
                    pairs.remove(t);
                    return;
                }
            }
        } else {
            targetted.sendMessage(Main.PREFIX + "Sorry, but you got no trading offer.");
        }
    }

    public Player cancelTrade(Player owner) {
        // returns opponent player
        if(pairs.containsKey(owner.getUniqueId())) {
            Player opposite = pairs.get(owner.getUniqueId());
            pairs.remove(owner.getUniqueId());
            return opposite;
        } else {
            return null;
        }
    }

    public void cancelOwnTrade(Player owner) {
        Player opposite = cancelTrade(owner);
        if(opposite != null) {
            owner.sendMessage(Main.PREFIX + "You cancelled your trade with " + opposite.getName() + "!");
            opposite.sendMessage(Main.PREFIX + owner.getName() + " canceled the trade with you.");
        } else {
            owner.sendMessage(Main.PREFIX + "Sorry, but you got no trade offers to cancel!");
        }
    }

    public void denyTrade(Player target) {
        boolean found = false;
        for(UUID key : pairs.keySet()) {
            if(pairs.get(key).equals(target)) {
                if(Bukkit.getPlayer(key) != null) {
                    Bukkit.getPlayer(key).sendMessage(Main.PREFIX + target.getName() +
                            " denied your trading request!");
                    target.sendMessage(Main.PREFIX + "Declined trade request by " +
                            Bukkit.getPlayer(key).getName());
                }
                pairs.remove(key);
            }
        }
        if(!found)
            target.sendMessage(Main.PREFIX + "You got no trade requests to deny!");
    }

    public void denyTrade(Player target, Player requester) {
        if(pairs.containsKey(requester.getUniqueId())) {
            requester.sendMessage(Main.PREFIX + target.getName() + " denied your trade request!");
            target.sendMessage(Main.PREFIX + "Declined trade request by " +
                    target.getName());
            pairs.remove(requester.getUniqueId());
            return;
        } else {
            target.sendMessage(Main.PREFIX + "You got no trade requests to deny!");
        }
    }

    public void addTradingWindow(TradingWindow tw) {
        this.currentDealInvs.add(tw);
    }

    public void removeTradingWindow(TradingWindow tw) {
        System.out.println("In removeTradingWindow() method");
        currentDealInvs.remove(tw);
    }

    public boolean isInventoryInList(Inventory inv) {
        if(inv != null) {
            for (TradingWindow c : currentDealInvs) {
                if (inv.equals(c.playerInventory) || inv.equals(c.oppositeInventory)) {
                    return true;
                }
            }
        }
        return false;
    }

    public TradingWindow getTradingWindow(Inventory inv) {
        if(inv != null) {
            for (TradingWindow c : currentDealInvs) {
                if (inv.equals(c.playerInventory) || inv.equals(c.oppositeInventory))
                    return c;
            }
        }
        return null;
    }

    public boolean isPlayerCurrentlyDealing(Player p) {
        for(TradingWindow c : currentDealInvs) {
            if(c.player.equals(p) || c.opposite.equals(p)) {
                return true;
            }
        }
        return false;
    }

    public TradingWindow getTradingWindowByPlayer(Player p) {
        for(TradingWindow c : currentDealInvs) {
            if(p.equals(c.player) || p.equals(c.opposite))
                return c;
        }
        return null;
    }

    public ArrayList<TradingWindow> getCurrentDealInvs() {
        return this.currentDealInvs;
    }

    public void closeAllTrades() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(this.isPlayerCurrentlyDealing(p)) {
                TradingWindow tw = this.getTradingWindowByPlayer(p);
                tw.closeTrade(p);
            }
        }
    }
}
