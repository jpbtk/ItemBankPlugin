package san.kuroinu.itembank.Commands;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static san.kuroinu.itembank.ItemBank.plugin;
import static san.kuroinu.itembank.ItemBank.prefix;

public class IB implements CommandExecutor {
    public IB() throws SQLException {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //コンソールからの入力だったらはじく
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix+"コンソールからの入力はできません");
            return true;
        }
        Player e = (Player) sender;
        //預けるか出すのwindowを開く
        if (args.length == 0 || args.length > 1) {
            sender.sendMessage(prefix+"使い方: /ib [inp/out]");
            return true;
        } else if (args[0].equals("inp")) {
            Inventory inv = Bukkit.createInventory(null, 27, prefix+"預けるアイテムを中に入れてください");
            e.openInventory(inv);
        }else if(args[0].equals("out")){
            Inventory inv = Bukkit.createInventory(null, 27, prefix+"出すアイテムをクリックしてください");
            //configのitemBankListを取得

            java.util.List<String> materialList = plugin.getConfig().getStringList("itemBankList");
            int count = materialList.size();
            String mcid = ((Player) sender).getUniqueId().toString();
            //count回繰り返す
            for (int i = 0; i < count; i++) {
                try {
                    inv.setItem(i,createGuiItem(Material.valueOf(materialList.get(i)), null, null, mcid));
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            e.openInventory(inv);
        }else{
            sender.sendMessage(prefix+"使い方: /ib [inp/out]");
        }
        return true;
    }

    //アイテムスタック作成
    protected ItemStack createGuiItem(final Material material, final String name, final String lore, final String uuid) throws SQLException {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        if (name != null) {
            meta.setDisplayName(name);
        }
        String item_name = material.toString();
        //item_nameが何個あるかを取得
        PreparedStatement ps = null;
        ResultSet result = null;
        try {
            ps = con.prepareStatement("SELECT * FROM mib WHERE item_mat = ? and mcid = ?");
            ps.setString(1, item_name);
            ps.setString(2, uuid);
            result = ps.executeQuery();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (result != null) {
                result.next();
                int count = result.getInt("count");
            ps.close();
                result.close();
                meta.setDisplayName(item_name+" x"+count);
        }
        // Set the lore of the item
        if (lore == null) {
            ArrayList<String> lores = new ArrayList<>();
            lores.add("右クリックで１個取り出し");
            lores.add("左クリックで1st取り出し");
            meta.setLore(lores);
        } else {
            meta.setLore(Arrays.asList(lore));
        }

        item.setItemMeta(meta);

        return item;
    }
    Connection con = DriverManager.getConnection(
            plugin.getConfig().getString("db.url"),
            plugin.getConfig().getString("db.user"),
            plugin.getConfig().getString("db.password")
    );
}
