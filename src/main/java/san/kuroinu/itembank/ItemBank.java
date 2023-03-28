package san.kuroinu.itembank;

import com.sun.tools.javac.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import san.kuroinu.itembank.Commands.IB;

import java.sql.SQLException;

public final class ItemBank extends JavaPlugin {

    public static JavaPlugin plugin;
    private Listeners listeners;

    public static String prefix = "§5[ItemBank]§r";
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        try {
            this.listeners = new Listeners();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Bukkit.getPluginManager().registerEvents(this.listeners, this);
        try {
            getCommand("ib").setExecutor(new IB());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        plugin.saveDefaultConfig();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        super.onDisable();
    }
    public static JavaPlugin getPlugin() {
        return plugin;
    }
}
