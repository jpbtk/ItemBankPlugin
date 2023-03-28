package san.kuroinu.itembank;

import com.sun.tools.javac.util.List;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;

import static san.kuroinu.itembank.ItemBank.plugin;
import static san.kuroinu.itembank.ItemBank.prefix;

public class Listeners implements Listener {
    public Listeners() throws SQLException {
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event){
        if (event.getView().getTitle().equals(prefix+"出すアイテムをクリックしてください")) {
            ArrayList<String> lores = new ArrayList<>();
            lores.add("右クリックで１個取り出し");
            lores.add("左クリックで1st取り出し");
            if (event.getCurrentItem() == null) {
                return;
            }
            if (event.getCurrentItem().getLore() == null) {
                return;
            }
            if (event.getCurrentItem().getItemMeta().getLore().equals(lores)){
                event.getWhoClicked().sendMessage(prefix+"アイテムを取り出そうとしています");
                //アイテムを取り出す処理
                PreparedStatement ps = null;
                String uuid = event.getWhoClicked().getUniqueId().toString();
                String item = event.getCurrentItem().getType().toString();
                try {
                    ps = con.prepareStatement("select * from mib where mcid = ? and item_mat = ?");
                    ps.setString(1, uuid);
                    ps.setString(2, item);
                    ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        //アイテムがある場合
                        int amount = result.getInt("count");
                        //右クリックか左クリックか判定する
                        if (event.isRightClick()){
                            //右クリックの場合
                            if (amount >= 1){
                                //アイテムが1個以上ある場合
                                ps = con.prepareStatement("update mib set count = ? where mcid = ? and item_mat = ?");
                                ps.setInt(1, amount - 1);
                                ps.setString(2, uuid);
                                ps.setString(3, item);
                                ps.executeUpdate();
                                ps.close();
                                ItemStack addItem = new ItemStack(Material.getMaterial(item), 1);
                                event.getWhoClicked().getInventory().addItem(addItem);
                                event.getWhoClicked().sendMessage(prefix+"アイテムを取り出しました");
                                event.setCancelled(true);
                                result.close();
                                ps.close();
                                return;
                            }
                        } else {
                            //左クリックの場合
                            if (amount >= 64){
                                //アイテムが64個以上ある場合
                                ps = con.prepareStatement("update mib set count = ? where mcid = ? and item_mat = ?");
                                ps.setInt(1, amount - 64);
                                ps.setString(2, uuid);
                                ps.setString(3, item);
                                ps.executeUpdate();
                                ItemStack addItem = new ItemStack(Material.getMaterial(item), 64);
                                event.getWhoClicked().getInventory().addItem(addItem);
                                event.getWhoClicked().sendMessage(prefix+"アイテムを取り出しました");
                                event.setCancelled(true);
                                result.close();
                                ps.close();
                                return;
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    event.getWhoClicked().sendMessage(prefix+"エラーが発生しました");
                }
                event.getWhoClicked().sendMessage(prefix+"アイテムがありません");
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void inpClose(InventoryCloseEvent event) throws SQLException {
        Inventory inv = event.getInventory();
        if (event.getView().getTitle().equals(prefix+"預けるアイテムを中に入れてください")) {
            //預ける処理
            //0から35で繰り返す
            PreparedStatement ps = null;
            java.util.List<String> itemBankList = plugin.getConfig().getStringList("itemBankList");
            String uuid = event.getPlayer().getUniqueId().toString();
            String item = null;
            for (int i = 0; i < 27; i++) {
                //アイテムがあるか確認
                if (inv.getItem(i) == null){
                    continue;
                }
                if (itemBankList.contains(inv.getItem(i).getType().toString())) {
                    item = inv.getItem(i).getType().toString();
                    ps = con.prepareStatement("select * from mib where mcid = ? and item_mat = ?");
                    ps.setString(1, uuid);
                    ps.setString(2, item);
                    ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        //アイテムがある場合
                        int amount = result.getInt("count");
                        amount += inv.getItem(i).getAmount();
                        ps = con.prepareStatement("update mib set count = ? where mcid = ? and item_mat = ?");
                        ps.setInt(1, amount);
                        ps.setString(2, uuid);
                        ps.setString(3, item);
                        ps.executeUpdate();
                        ps.close();
                    } else {
                        //アイテムがない場合
                        ps = con.prepareStatement("insert into mib (mcid, item_mat, count) values (?, ?, ?)");
                        ps.setString(1, uuid);
                        ps.setString(2, item);
                        ps.setInt(3, inv.getItem(i).getAmount());
                        ps.executeUpdate();
                        ps.close();
                    }

                    event.getPlayer().sendMessage(prefix+item+"を"+inv.getItem(i).getAmount()+"個預けました");
                }
            }
        }
    }

    //データベースに接続
    Connection con = DriverManager.getConnection(
            plugin.getConfig().getString("db.url"),
            plugin.getConfig().getString("db.user"),
            plugin.getConfig().getString("db.password")
    );
}
