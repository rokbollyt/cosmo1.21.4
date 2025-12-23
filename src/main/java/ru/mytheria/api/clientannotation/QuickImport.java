package ru.mytheria.api.clientannotation;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Tessellator;
import ru.mytheria.Mytheria;

public interface QuickImport {
    MinecraftClient mc = MinecraftClient.getInstance();
    //Tessellator tessellator = Tessellator.getInstance();
    Mytheria client = Mytheria.getInstance();

    Gson gson = new Gson();
}
