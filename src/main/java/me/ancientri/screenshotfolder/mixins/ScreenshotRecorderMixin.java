package me.ancientri.screenshotfolder.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.ancientri.screenshotfolder.ScreenshotFolder;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {
	@Shadow
	private static File getScreenshotFilename(File directory) { return directory; } // Dummy method to avoid compilation errors

	@Inject(method = "method_68157", at = @At(value = "INVOKE", target = "Ljava/io/File;mkdir()Z"))
	private static void modifyScreenshotsDir(File file, String string, Consumer<Text> consumer, NativeImage image, CallbackInfo ci, @Local(name = "file2") LocalRef<File> file2) {
		ScreenshotFolder.mixinBody(ScreenshotRecorderMixin::getScreenshotFilename, file2);
	}
}
