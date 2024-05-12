package soys.doublemineral;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends JavaPlugin implements Listener {
    public static Main plugin;
    public static boolean testMod=false;
    @Override
    public void onEnable() {
        plugin=this;
        Bukkit.getPluginManager().registerEvents(this,this);
        sendPluginText("时运 矿物块事件注册完毕");
        saveDefaultConfig();
        sendPluginText("成功加载配置文件");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp()){
            return true;
        }
        if(args.length==1){
            if(args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                sendPluginText("成功加载配置文件");
            }
            if(args[0].equalsIgnoreCase("testmod")){
                testMod=!testMod;
                sendPluginText("测试模式信息输出至后台,测试模式修改为 :" +testMod);
            }
        }
        return true;
    }


    public boolean isConfigBlock(Block block){
        int id=block.getType().getId();
        byte damage=block.getData();
        sendTestModPluginText("damage: "+damage);
        String idDamage= String.valueOf(id);
        if(damage!=0){
            idDamage=idDamage+":"+damage;
        }
        sendTestModPluginText("idDamage: "+idDamage);
        if(!getConfig().getStringList("block").contains(idDamage)){
            sendTestModPluginText("不存在该匹配: "+idDamage);
            return false;
        }
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e){
        sendTestModPluginText("破坏开始");
        Block block=e.getBlock();
        if(block.getType()== Material.AIR){
            return;
        }
        if(block.hasMetadata(getName())){
            return;
        }

        block.setMetadata(getName(),new FixedMetadataValue(plugin,true));
        BlockBreakEvent blockBreakEvent=new BlockBreakEvent(block,e.getPlayer());
        Bukkit.getPluginManager().callEvent(blockBreakEvent);
        if(blockBreakEvent.isCancelled()){
            return;
        }
        block.removeMetadata(getName(),plugin);

        WorldNBTFile worldNBTFile=new WorldNBTFile(block.getWorld());
        String locString=getLoctionString(block.getLocation());
        boolean isHumanPlace = worldNBTFile.getBoolean(locString);
        sendTestModPluginText("getNbt(): "+worldNBTFile.getNbt().toString());
        sendTestModPluginText("isHumanPlace: "+isHumanPlace);
        if(isHumanPlace){
            worldNBTFile.remove(locString);
            try {
                worldNBTFile.save();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return;
        }
        if(!isConfigBlock(block)){
            return;
        }
        sendTestModPluginText("方块存在: "+block.getType().getId());

        Player player=e.getPlayer();
        if(player==null){
            return;
        }
        sendTestModPluginText("玩家存在");

        ItemStack itemStack=player.getItemInHand();
        if(itemStack==null){
            return;
        }
        sendTestModPluginText("手持物品存在");

        Map<Enchantment, Integer> enchantmentIntegerMap=itemStack.getEnchantments();
        if(enchantmentIntegerMap.isEmpty()){
            return;
        }
        sendTestModPluginText("附魔存在");

        AtomicInteger randomBlock= new AtomicInteger(0);
        enchantmentIntegerMap.forEach((enchantment, integer) -> {
            if(enchantment.getId()==35){
                randomBlock.set((int) (Math.random() * integer));
            }
        });
        int randomCount=randomBlock.get();
        sendTestModPluginText("随机掉落: "+randomCount);

        if(randomCount>0){
            ItemStack itemBlock= block.getState().getData().toItemStack(randomCount);
            //ItemStack itemBlock=new ItemStack(block.getType(),randomCount,block.getData());
            block.getWorld().dropItem(e.getBlock().getLocation(), itemBlock);
            sendTestModPluginText("随机掉落成功 ");
            return;
        }
        sendTestModPluginText("掉落数量为 0 ");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e){
        Block block=e.getBlock();
        if(!isConfigBlock(block)){
            return;
        }
        sendTestModPluginText("放置存在方块");
        Player player=e.getPlayer();
        if(player==null){
            return;
        }
        sendTestModPluginText("放置玩家存在");
        WorldNBTFile worldNBTFile=new WorldNBTFile(block.getWorld());
        String locString=getLoctionString(block.getLocation());
        worldNBTFile.setBoolean(locString,true);
        try {
            worldNBTFile.save();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        //nbtTileEntity.setBoolean("humanPlace",true);
        sendTestModPluginText("禁止时运方块");
    }

    public static String getLoctionString(Location location){
        StringBuilder builder=new StringBuilder();
        builder.append(location.getWorld().getName());
        builder.append(",");
        builder.append(location.getBlockX());
        builder.append(",");
        builder.append(location.getBlockY());
        builder.append(",");
        builder.append(location.getBlockZ());
        return builder.toString();
    }

    public static void sendTestModPluginText(String message){
        if(testMod) {
            Bukkit.getLogger().info("[" + plugin.getName() + "][TESTMOD] -> " + message);
        }
    }

    public static void sendPluginText(String message){
        Bukkit.getLogger().info("["+plugin.getName()+"]"+message);
    }
}
