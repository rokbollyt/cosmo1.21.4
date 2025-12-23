package ru.mytheria.api.util.color;




import net.minecraft.util.math.ColorHelper;
import org.lwjgl.opengl.GL11;
import ru.mytheria.api.util.shader.common.states.QuadColorState;

import java.nio.ByteBuffer;

public class ColorUtil extends ColorHelper {

    public static int applyOpacity(int hex, int percent) {
        return applyOpacity(hex, 2.55f * Math.min(percent, 100));
    }

    public static int applyOpacity(int hex, float opacity) {
        return ColorHelper.getArgb((int) (ColorHelper.getAlpha(hex) * (opacity / 255)), ColorHelper.getRed(hex), ColorHelper.getGreen(hex), ColorHelper.getBlue(hex));
    }

    public static QuadColorState applyOpacity( QuadColorState colorState, int opacity) {
        return new QuadColorState(ColorHelper.withAlpha(opacity, colorState.color1()),
                ColorHelper.withAlpha(opacity, colorState.color2()),
                ColorHelper.withAlpha(opacity, colorState.color3()),
                ColorHelper.withAlpha(opacity, colorState.color4()));
    }

    public static int lerp(float value, int from, int to) {
        return ColorHelper.lerp(value, from, to);
    }

    public static int pixelColor(int x, int y) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);

        GL11.glReadPixels(x, y, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);

        return ColorHelper.getArgb(getRed(byteBuffer.get()), getGreen(byteBuffer.get()), getBlue(byteBuffer.get()));
    }

}
