package nl.dizmizzer.wb;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 * Created by DizMizzer.
 * Users don't have permission to release
 * the code unless stated by the Developer.
 * You are allowed to copy the source code
 * and edit it in any way, but not distribute
 * it. If you want to distribute addons,
 * please use the API. If you can't access
 * a certain thing in the API, please contact
 * the developer.
 */

public class Main extends JavaPlugin implements Listener {

    HashMap<String, Integer[]> borders = new HashMap<>();
    ArrayList<UUID> players = new ArrayList<>();
    public void onEnable() {
        try {
            setup();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fillBorders();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void fillBorders() {
        for (String s : config.getKeys(false)) {
            int x = config.getInt(s + ".x");
            int z = config.getInt(s + ".z");
            int dx = config.getInt(s + ".dx");
            int dz = config.getInt(s + ".dz");

            Integer[] m = {x + dx, z + dz, x - dx, z - dz};
            borders.put(s, m);
        }
    }

    public void onDisable() {
        saveData();
    }

    File file = null;
    FileConfiguration config = null;
    private void setup() throws IOException {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        file = new File(getDataFolder(), "data.yml");
        if (! file.exists()) {
            file.createNewFile();
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    private void saveData() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("borderset")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(toColor("&cOnly players can set the border, you silly!"));
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(toColor("&/borderset <x> <z>"));
                return true;
            }
            Player player = (Player) sender;

            int dx = 0;
            int dz = 0;
            try {
                dx = Integer.parseInt(args[0]);
                dz = Integer.parseInt(args[1]);
            } catch (Exception e) {
                player.sendMessage(toColor("&cPlease use numbers for x and z values."));
                return true;
            }

            config.set(player.getLocation().getWorld().getName() + ".x",player.getLocation().getBlockX());
            config.set(player.getLocation().getWorld().getName() + ".z",player.getLocation().getBlockZ());
            config.set(player.getLocation().getWorld().getName() + ".dx",dx);
            config.set(player.getLocation().getWorld().getName() + ".dz",dz);

            borders.put(player.getLocation().getWorld().getName(), new Integer[]{player.getLocation().getBlockX() + dx, player.getLocation().getBlockZ() + dz, player.getLocation().getBlockX() - dx, player.getLocation().getBlockZ() - dz});
            saveData();
            sender.sendMessage(toColor(ChatColor.GREEN + "Succesfully added border!"));
            return true;

        }


        return true;
    }

    private String toColor(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (players.contains(e.getPlayer().getUniqueId())) players.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(e.getTo().distanceSquared(e.getFrom()) == 0) return;
        if (!config.contains(e.getTo().getWorld().getName())) return;

        int x = e.getTo().getBlockX();
        int z = e.getTo().getBlockZ();
        String world = e.getTo().getWorld().getName();
        if (x > borders.get(world)[0]) {
            e.getPlayer().setVelocity(new Vector(0,0,0));
            e.getPlayer().setVelocity(new Vector(-0.8D, 0.6D, 0.0D));
        } else if (x < borders.get(world)[2]) {
            e.getPlayer().setVelocity(new Vector(0,0,0));
            e.getPlayer().setVelocity(new Vector(0.8D, 0.6D, 0.0D));

        } else if (z > borders.get(world)[1]) {
            e.getPlayer().setVelocity(new Vector(0,0,0));
            e.getPlayer().setVelocity(new Vector(0.0D, 0.6D, -0.8D));

        } else if (z < borders.get(world)[3]) {
            e.getPlayer().setVelocity(new Vector(0,0,0));
            e.getPlayer().setVelocity(new Vector(0.0D, 0.6D, 0.8D));

        } else {
            return;
        }
        e.getPlayer().sendMessage(toColor("&cYou crossed the border!"));
        if (! players.contains(e.getPlayer().getUniqueId()))         players.add(e.getPlayer().getUniqueId());

    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (e.getCause() == DamageCause.FALL && players.contains(p.getUniqueId())) {
                e.setDamage(0.0);
                players.remove(p.getUniqueId());
            }
        }
    }

}
