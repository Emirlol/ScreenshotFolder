package me.rime.screenshotfolder.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.rime.screenshotfolder.config.ConfigHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
@Debug(export = true)
@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {
	@Shadow
	private static File getScreenshotFilename(File directory) { return directory; } // Dummy method to avoid compilation errors

	@Inject(method = "method_1661", at = @At("HEAD"))
	private static void modifyScreenshotsDir(NativeImage nativeImage, File file, Consumer<Text> consumer, CallbackInfo ci, @Local(argsOnly = true) LocalRef<File> file2) {
		if (ConfigHandler.INSTANCE.getEnabled().get().equals(Boolean.FALSE)) return;
		Path actualDir = ConfigHandler.INSTANCE.getActualDir();
		if (actualDir == null) return;

		if (!Files.isDirectory(actualDir)) {
			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("The selected screenshot path is not a directory!"));
			return;
		}

		if (Files.notExists(actualDir)) {
			try {
				Files.createDirectories(actualDir);
			} catch (Exception e) {
				e.printStackTrace();
				MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Failed to create the selected screenshot path!"));
			}
		}

		File imageFile = getScreenshotFilename(actualDir.toFile());
		file2.set(imageFile);
	}
}
