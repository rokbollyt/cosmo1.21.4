package fun.cosmo.api.clientannotation;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import fun.cosmo.Mytheria;

public interface QuickImport {
    MinecraftClient mc = MinecraftClient.getInstance();
    //Tessellator tessellator = Tessellator.getInstance();
    Mytheria client = Mytheria.getInstance();

    Gson gson = new Gson();
}
