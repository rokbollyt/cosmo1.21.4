package fun.cosmo.api.events.impl;


import lombok.*;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import fun.cosmo.api.events.Event;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class Render2DEvent extends Event {
    MatrixStack matrixStack;
    RenderTickCounter tickDelta;
    int screenWidth;
    int screenHeight;
}