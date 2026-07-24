package com.apjiah.simplehomes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Simplehomes extends JavaPlugin {

    private File homesFile;
    private FileConfiguration homesConfig;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveDefaultConfig();
        config = getConfig();

        homesFile = new File(getDataFolder(), "homes.yml");
        if (!homesFile.exists()) {
            try {
                homesFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Не удалось создать homes.yml!");
                e.printStackTrace();
            }
        }
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);

        getCommand("sethome").setExecutor(this);
        getCommand("home").setExecutor(this);
        getCommand("delhome").setExecutor(this);

        getLogger().info("Simplehomes включён!");
    }

    @Override
    public void onDisable() {
        saveHomes();
        getLogger().info("Simplehomes выключён!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("simplehomes.use")) {
            player.sendMessage(getMessage("no-permission"));
            return true;
        }

        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "sethome":
                return setHome(player);
            case "home":
                return teleportHome(player);
            case "delhome":
                return deleteHome(player);
            default:
                return false;
        }
    }

    private boolean setHome(Player player) {
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();

        String path = uuid.toString();
        homesConfig.set(path + ".world", loc.getWorld().getName());
        homesConfig.set(path + ".x", loc.getX());
        homesConfig.set(path + ".y", loc.getY());
        homesConfig.set(path + ".z", loc.getZ());
        homesConfig.set(path + ".yaw", (double) loc.getYaw());
        homesConfig.set(path + ".pitch", (double) loc.getPitch());

        saveHomes();
        player.sendMessage(getMessage("home-set"));
        return true;
    }

    private boolean teleportHome(Player player) {
        UUID uuid = player.getUniqueId();
        String path = uuid.toString();

        if (!homesConfig.contains(path)) {
            player.sendMessage(getMessage("no-home"));
            return true;
        }

        try {
            String worldName = homesConfig.getString(path + ".world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage(getMessage("world-not-found"));
                return true;
            }

            double x = homesConfig.getDouble(path + ".x");
            double y = homesConfig.getDouble(path + ".y");
            double z = homesConfig.getDouble(path + ".z");
            float yaw = (float) homesConfig.getDouble(path + ".yaw");
            float pitch = (float) homesConfig.getDouble(path + ".pitch");

            Location homeLoc = new Location(world, x, y, z, yaw, pitch);
            player.teleport(homeLoc);
            player.sendMessage(getMessage("teleported"));
        } catch (Exception e) {
            player.sendMessage(getMessage("error"));
            getLogger().warning("Ошибка телепортации игрока " + player.getName() + ": " + e.getMessage());
        }
        return true;
    }

    private boolean deleteHome(Player player) {
        UUID uuid = player.getUniqueId();
        String path = uuid.toString();

        if (!homesConfig.contains(path)) {
            player.sendMessage(getMessage("no-home"));
            return true;
        }

        homesConfig.set(path, null);
        saveHomes();
        player.sendMessage(getMessage("home-deleted"));
        return true;
    }

    private void saveHomes() {
        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            getLogger().severe("Не удалось сохранить homes.yml!");
            e.printStackTrace();
        }
    }

    private String getMessage(String key) {
        String message = config.getString("messages." + key, "&cСообщение не найдено: " + key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}