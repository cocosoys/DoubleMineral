package soys.doublemineral;

import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.World;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class WorldNBTFile {
    public World world;
    public NBTTagCompound nbt;
    public File blockFile;
    public WorldNBTFile(World world){
        this.world=world;
        File worldFile=world.getWorldFolder();
        File blockFile=new File(worldFile,"block.dat");
        this.blockFile=blockFile;
        try {
            if(!blockFile.exists()){
                nbt=new NBTTagCompound();
                save();
            }
            update();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public NBTTagCompound getNbt() {
        return nbt;
    }

    public void setBoolean(String name,boolean val){
        getNbt().setBoolean(name,val);
    }

    public void remove(String name){
        getNbt().remove(name);
    }

    public boolean getBoolean(String name){
        return getNbt().getBoolean(name);
    }

    public void save() throws IOException {
        NBTCompressedStreamTools.a(getNbt(),Files.newOutputStream(blockFile.toPath()));
    }

    public void update() throws IOException {
        this.nbt = NBTCompressedStreamTools.a(Files.newInputStream(blockFile.toPath()));
    }
}
