package us.potatoboy.headindex.mixin;

import com.mojang.authlib.yggdrasil.YggdrasilGameProfileRepository;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = YggdrasilGameProfileRepository.class, remap = false)
public class YggdrasilGameProfileRepositoryMixin {
    @Redirect(method = "findProfileByName", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void preventLogging(Logger instance, String s, Object o, Object o2) {}
}
