package io.github.indicode.fabric.informedload.mixin;

import io.github.indicode.fabric.informedload.InformedLoadUtils;
import com.google.gson.JsonParser;
import io.github.indicode.fabric.informedload.TaskList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.FontType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Indigo Amann
 */
@Mixin(SplashScreen.class)
public abstract class SplashMixin extends Overlay {
    @Shadow
    private MinecraftClient client;
    @Shadow
    private float field_17770;
    @Shadow
    private void renderProgressBar(int int_1, int int_2, int int_3, int int_4, float float_1, float float_2) {}
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/SplashScreen;blit(IIIIII)V"))
    public void translateLogo(SplashScreen dis, int x, int y, int idk1, int idk2, int idk3, int idk4) {
        blit(x, y - 40, idk1, idk2, idk3,  idk4);
    }
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/SplashScreen;renderProgressBar(IIIIFF)V"))
    public void swapProgressRender(SplashScreen dis, int x, int y, int end_x, int end_y, float progress, float fadeAmount) {
        int window_width = this.client.window.getScaledWidth();
        int window_height = this.client.window.getScaledHeight();
        y = window_height / 4 * 3 - 40;
        int middle_x = window_width / 2;

        String status = "Loading";

        if (!TaskList.isEmpty()) {
            Iterator iterator = TaskList.iterator();
            while (iterator.hasNext()) {
                status += " - " + ((TaskList.Task) iterator.next()).name;
            }
        }
        InformedLoadUtils.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, this.field_17770, status, fadeAmount, false);
        y += 20;
        if (!TaskList.isEmpty()) {
            Iterator iterator = TaskList.iterator();
            while (iterator.hasNext()) {
                y = ((TaskList.Task)iterator.next()).render(y, middle_x, window_width, window_height, fadeAmount);
            }
        }
    }
    @Inject(method = "<init>", at = @At("RETURN"))
    public void setup(CallbackInfo ci) {
        if (InformedLoadUtils.textRenderer == null) {
            MinecraftClient client = MinecraftClient.getInstance();
            final FontStorage fontStorage_1 = new FontStorage(client.getTextureManager(), new Identifier("loading"));
            fontStorage_1.setFonts(Collections.singletonList(FontType.BITMAP.createLoader(new JsonParser().parse(InformedLoadUtils.FONT_JSON).getAsJsonObject()).load(client.getResourceManager())));
            InformedLoadUtils.textRenderer = new TextRenderer(client.getTextureManager(), fontStorage_1);
        }
        InformedLoadUtils.renderProgressBar = (params) -> {
            renderProgressBar((int) params[0], (int) params[1], (int) params[2], (int) params[3], (float) params[4], (float) params[5]);
        };
    }
}