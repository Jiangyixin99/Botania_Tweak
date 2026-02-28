package com.prunoideae.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.block.block_entity.CraftyCrateBlockEntity;

@Mixin(CraftyCrateBlockEntity.class)
public abstract class MixinCraftyCrateBlockEntity implements ManaReceiver {

    protected Container getInventory() {
        // Shadow方法占位符
        throw new AssertionError();
    }

    @Shadow(remap = false)
    private boolean isLocked(int slot) {
        return false;
    }

    @Unique
    private int currentMana = 0;

    @Unique
    private static final int MAX_MANA = 2000;

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true, remap = false)
    private void onCraft(boolean fullCheck, CallbackInfoReturnable<Boolean> cir) {
        // 计算机巧箱内非空且未锁定的物品数量
        int itemCount = 0;
        var handler = getInventory();
        for (int i = 0; i < handler.getContainerSize(); i++) {
            if (!handler.getItem(i).isEmpty() && !isLocked(i)) {
                itemCount++;
            }
        }

        // 计算所需魔力（最大 2000，按物品数量比例消耗）
        // 例如：9个物品消耗2000x90%=1800魔力，1个物品消耗2000x10%=200魔力
        float percentage = itemCount / 9.0f;
        // 确保至少消耗10%的魔力（即使只有1个物品）
        percentage = Math.max(percentage, 0.1f);
        int requiredMana = (int) (MAX_MANA * percentage);

        if (currentMana < requiredMana) {
            cir.setReturnValue(false); // 魔力不足，取消合成
            return;
        }

        // 扣除魔力并标记更新
        currentMana -= requiredMana;
        setChanged();
    }

    @Inject(method = "writePacketNBT", at = @At("RETURN"), remap = false)
    private void onWritePacketNBT(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("botaniatweaks:mana", currentMana);
    }

    @Inject(method = "readPacketNBT", at = @At("RETURN"), remap = false)
    private void onReadPacketNBT(CompoundTag tag, CallbackInfo ci) {
        currentMana = tag.getInt("botaniatweaks:mana");
    }

    // ========== ManaReceiver 接口实现 ==========

    @Override
    public Level getManaReceiverLevel() {
        return ((CraftyCrateBlockEntity) (Object) this).getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return ((CraftyCrateBlockEntity) (Object) this).getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return currentMana;
    }

    @Override
    public boolean isFull() {
        return currentMana >= MAX_MANA;
    }

    @Override
    public void receiveMana(int mana) {
        currentMana = Math.min(currentMana + mana, MAX_MANA);
        setChanged();
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true; // 允许接收魔力脉冲
    }

    @Unique
    private void setChanged() {
        ((CraftyCrateBlockEntity) (Object) this).setChanged();
    }
}