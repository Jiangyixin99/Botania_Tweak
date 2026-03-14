package com.prunoideae.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
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

        // 绘制物品 GUI
        renderItemGui(gui, mc, xc, manaBarY + 12);
    }

    private void renderItemGui(GuiGraphics gui, Minecraft mc, int xc, int yStart) {
        // 尝试获取机巧箱的物品容器
        Container inventory = null;
        try {
            // 通过反射获取 inventory 字段
            java.lang.reflect.Field inventoryField = CraftyCrateBlockEntity.class.getDeclaredField("inventory");
            inventoryField.setAccessible(true);
            inventory = (Container) inventoryField.get(crate);
        } catch (Exception e) {
            // 如果反射失败，尝试其他方式
            try {
                // 尝试调用 getInventory 方法
                java.lang.reflect.Method getInventoryMethod = CraftyCrateBlockEntity.class.getDeclaredMethod("getInventory");
                getInventoryMethod.setAccessible(true);
                inventory = (Container) getInventoryMethod.invoke(crate);
            } catch (Exception ex) {
                // 如果都失败了，就不显示物品 GUI
                return;
            }
        }

        if (inventory != null) {
            int slotSize = 18;
            int slotsPerRow = 9;
            int guiWidth = slotsPerRow * slotSize;
            int guiX = xc + (52 - guiWidth) / 2;
            int guiY = yStart;

            // 绘制背景
            gui.fill(guiX - 2, guiY - 2, guiX + guiWidth + 2, guiY + 3 * slotSize + 2, 0x80000000);
            gui.fill(guiX - 1, guiY - 1, guiX + guiWidth + 1, guiY + 3 * slotSize + 1, 0xFF888888);
            gui.fill(guiX, guiY, guiX + guiWidth, guiY + 3 * slotSize, 0xFF000000);

            // 绘制物品槽位和物品
            for (int i = 0; i < Math.min(inventory.getContainerSize(), 27); i++) {
                int row = i / slotsPerRow;
                int col = i % slotsPerRow;
                int slotX = guiX + col * slotSize;
                int slotY = guiY + row * slotSize;

                // 绘制槽位边框
                gui.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xFF444444);
                gui.fill(slotX + 1, slotY + 1, slotX + slotSize - 1, slotY + slotSize - 1, 0xFF666666);

                // 绘制物品
                ItemStack stack = inventory.getItem(i);
                if (!stack.isEmpty()) {
                    gui.renderItem(stack, slotX + 1, slotY + 1);
                    gui.renderItemDecorations(mc.font, stack, slotX + 1, slotY + 1);
                }
            }
        }
    }
}