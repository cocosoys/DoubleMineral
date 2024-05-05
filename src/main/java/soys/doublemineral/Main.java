package soys.doublemineral;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends JavaPlugin implements Listener {
    public static Main plugin;
    public static boolean testMod;
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
        return false;
    }


    public boolean isConfigBlock(Block block){
        int id=block.getType().getId();
        short damage=block.getType().getMaxDurability();
        String idDamage= String.valueOf(id);
        if(damage!=0){
            idDamage=idDamage+":"+damage;
        }
        if(!getConfig().getStringList("block").contains(idDamage)){
            return false;
        }
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        sendTestModPluginText("破坏开始");
        Block block=e.getBlock();
        if(block.hasMetadata("humanPlace")){
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
            ItemStack itemBlock=new ItemStack(block.getType(),randomCount,block.getType().getMaxDurability());
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
        e.getBlock().setMetadata("humanPlace",new FixedMetadataValue(this,true));
        sendTestModPluginText("禁止时运方块");
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
