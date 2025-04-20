package mypals.ml.mixin.carpet;

import carpet.api.settings.SettingsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;
import java.util.Map;

@Mixin(SettingsManager.class)
public interface SettingsManagerAccessor {
    // 使用 @Invoker 创建一个调用私有方法的接口方法
    /*@Invoker("readSettingsFromConf")
    SettingsManager.ConfigReadResult invokeReadSettingsFromConf(Path path);*/
}
