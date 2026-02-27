package com.prunoideae.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.block_entity.CraftyCrateBlockEntity;

@Mixin(vazkii.botania.common.block.block_entity.CraftyCrateBlockEntity.WandHud.class)
public class MixinCraftyCrateWandHud {

    @Shadow(remap = false)
    @Final
    private CraftyCrateBlockEntity crate;

    @Inject(method = "renderHUD", at = @At("RETURN"), remap = false)
    private void onRenderHUD(GuiGraphics gui, Minecraft mc, CallbackInfo ci) {
        // 获取当前魔力值（需要从 BlockEntity 获取）
        int currentMana = 0;
        if (crate instanceof ManaReceiver) {
            currentMana = ((ManaReceiver) crate).getCurrentMana();
        }

        int maxMana = 2000;

        // 计算魔力条位置
        int xc = mc.getWindow().getGuiScaledWidth() / 2 + 12;
        int yc = mc.getWindow().getGuiScaledHeight() / 2 - 26;
        int width = 52;
        int height = 52;

        int manaBarWidth = 103;
        int manaBarX = xc + (width - manaBarWidth) / 2;
        int manaBarY = yc + height + 8;

        // 绘制魔力条
        HUDHandler.renderManaBar(gui, manaBarX, manaBarY, 0x4444FF, 1.0F, currentMana, maxMana);
    }
}