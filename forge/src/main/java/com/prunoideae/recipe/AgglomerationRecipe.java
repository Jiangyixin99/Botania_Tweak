package com.prunoideae.recipe;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AgglomerationRecipe {
    public final ImmutableList<ItemStack> recipeStacks;
    public final ImmutableList<TagKey<Item>> recipeItemTags;
    public final ImmutableList<ItemStack> recipeOutputs; // 多输出
    public final int manaCost;
    public final BlockState multiblockCenter;
    public final BlockState multiblockEdge;
    public final BlockState multiblockCorner;
    @Nullable
    public final BlockState multiblockCenterReplace;
    @Nullable
    public final BlockState multiblockEdgeReplace;
    @Nullable
    public final BlockState multiblockCornerReplace;

    final int totalInputs;

    public AgglomerationRecipe(ImmutableList<Object> recipeInputs,
                               ImmutableList<ItemStack> recipeOutputs, // 注意：这里可以是 ItemStack 或 TagKey？但通常输出都是 ItemStack，建议用 ImmutableList<ItemStack>
                               int manaCost,
                               BlockState multiblockCenter, BlockState multiblockEdge, BlockState multiblockCorner,
                               @Nullable BlockState multiblockCenterReplace,
                               @Nullable BlockState multiblockEdgeReplace,
                               @Nullable BlockState multiblockCornerReplace) {
        verifyInputs(recipeInputs);

        // 处理输入
        ImmutableList.Builder<ItemStack> stackInputBuilder = ImmutableList.builder();
        ImmutableList.Builder<TagKey<Item>> tagInputBuilder = ImmutableList.builder();
        for (Object o : recipeInputs) {
            if (o instanceof ItemStack) {
                ItemStack stack = ((ItemStack) o).copy();
                stack.setCount(1);
                stackInputBuilder.add(stack);
            } else if (o instanceof TagKey<?>) {
                @SuppressWarnings("unchecked")
                TagKey<Item> tag = (TagKey<Item>) o;
                tagInputBuilder.add(tag);
            }
        }
        this.recipeStacks = stackInputBuilder.build();
        this.recipeItemTags = tagInputBuilder.build();
        this.totalInputs = recipeStacks.size() + recipeItemTags.size();

        // 处理输出（所有输出应为 ItemStack，忽略 TagKey）
        ImmutableList.Builder<ItemStack> outputBuilder = ImmutableList.builder();
        for (Object o : recipeOutputs) {
            if (o instanceof ItemStack) {
                // 直接复制，保留原始 count
                outputBuilder.add(((ItemStack) o).copy());
            } else {
                // 如果输出包含 TagKey，可以忽略或抛出异常，这里简单忽略
                // 实际使用中输出通常都是具体物品
            }
        }
        this.recipeOutputs = outputBuilder.build();

        this.manaCost = manaCost;
        this.multiblockCenter = multiblockCenter;
        this.multiblockEdge = multiblockEdge;
        this.multiblockCorner = multiblockCorner;
        this.multiblockCenterReplace = multiblockCenterReplace;
        this.multiblockEdgeReplace = multiblockEdgeReplace;
        this.multiblockCornerReplace = multiblockCornerReplace;
    }

    // ========== 辅助方法 ==========

    /**
     * 获取第一个输出物品的副本（用于与原版接口兼容）
     */
    public ItemStack getPrimaryOutputCopy() {
        return recipeOutputs.isEmpty() ? ItemStack.EMPTY : recipeOutputs.get(0).copy();
    }

    /**
     * 获取所有输出物品的副本列表
     */
    public List<ItemStack> getOutputCopies() {
        List<ItemStack> copies = new ArrayList<>();
        for (ItemStack stack : recipeOutputs) {
            copies.add(stack.copy());
        }
        return copies;
    }

    // ========== 结构匹配 ==========

    private void verifyInputs(ImmutableList<Object> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("Can't make empty agglomeration recipe");
        }
        for (Object o : inputs) {
            if (o instanceof ItemStack || o instanceof TagKey) continue;
            throw new IllegalArgumentException("Illegal recipe input: " + o);
        }
    }

    public boolean structureMatches(Level level, BlockPos platePos,
                                    BlockState below, BlockState side, BlockState corner) {
        return multiblockMatches(level, platePos, below, side, corner);
    }

    public boolean multiblockMatches(Level level, BlockPos platePos,
                                     BlockState belowEarly, BlockState edgeEarly, BlockState cornerEarly) {
        if (!areStatesSimilar(belowEarly, multiblockCenter)) return false;
        if (!areStatesSimilar(edgeEarly, multiblockEdge)) return false;
        if (!areStatesSimilar(cornerEarly, multiblockCorner)) return false;

        BlockPos centerPos = platePos.below();
        Direction[] sides = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction dir : sides) {
            BlockPos edgePos = centerPos.relative(dir);
            if (!areStatesSimilar(level.getBlockState(edgePos), multiblockEdge)) return false;

            Direction left = dir.getCounterClockWise();
            Direction right = dir.getClockWise();
            BlockPos corner1 = edgePos.relative(left);
            BlockPos corner2 = edgePos.relative(right);
            if (!areStatesSimilar(level.getBlockState(corner1), multiblockCorner)) return false;
            if (!areStatesSimilar(level.getBlockState(corner2), multiblockCorner)) return false;
        }
        return true;
    }

    private boolean areStatesSimilar(BlockState a, BlockState b) {
        // 如果涉及水，必须都是水源（默认状态）
        if (a.getBlock() == Blocks.WATER || b.getBlock() == Blocks.WATER) {
            return a.getBlock() == Blocks.WATER
                    && b.getBlock() == Blocks.WATER
                    && a == Blocks.WATER.defaultBlockState()
                    && b == Blocks.WATER.defaultBlockState();
        }
        // 如果涉及熔岩，必须都是熔岩源（默认状态）
        if (a.getBlock() == Blocks.LAVA || b.getBlock() == Blocks.LAVA) {
            return a.getBlock() == Blocks.LAVA
                    && b.getBlock() == Blocks.LAVA
                    && a == Blocks.LAVA.defaultBlockState()
                    && b == Blocks.LAVA.defaultBlockState();
        }
        // 对于其他方块，只比较方块类型（忽略方向等属性）
        return a.getBlock() == b.getBlock();
    }
    // ========== 物品匹配 ==========

    private BlockState equalizeDirectionProperties(BlockState state) {
        if (state.hasProperty(DirectionalBlock.FACING)) {
            return state.setValue(DirectionalBlock.FACING, Direction.NORTH);
        }
        for (Property<?> prop : state.getProperties()) {
            if (prop.getValueClass() == Direction.class) {
                @SuppressWarnings("unchecked")
                Property<Direction> dirProp = (Property<Direction>) prop;
                if (state.getValue(dirProp) != Direction.NORTH) {
                    return state.setValue(dirProp, Direction.NORTH);
                }
            }
        }
        return state;
    }

    public boolean itemsMatch(List<ItemStack> flattenedInputs) {
        if (flattenedInputs.size() != totalInputs) return false;

        boolean[] used = new boolean[flattenedInputs.size()];
        int matched = 0;

        for (ItemStack recipeStack : recipeStacks) {
            boolean found = false;
            for (int i = 0; i < flattenedInputs.size(); i++) {
                if (used[i]) continue;
                ItemStack input = flattenedInputs.get(i);
                if (ItemStack.isSameItemSameTags(recipeStack, input)) {
                    used[i] = true;
                    found = true;
                    matched++;
                    break;
                }
            }
            if (!found) return false;
        }

        for (TagKey<Item> tag : recipeItemTags) {
            boolean found = false;
            for (int i = 0; i < flattenedInputs.size(); i++) {
                if (used[i]) continue;
                ItemStack input = flattenedInputs.get(i);
                if (input.is(tag)) {
                    used[i] = true;
                    found = true;
                    matched++;
                    break;
                }
            }
            if (!found) return false;
        }

        return matched == totalInputs;
    }

    public boolean matches(Level level, BlockPos platePos, List<ItemStack> flattenedInputs,
                           BlockState below, BlockState side, BlockState corner) {
        return structureMatches(level, platePos, below, side, corner) && itemsMatch(flattenedInputs);
    }

    // ========== 标准方法 ==========

    public ImmutableList<ItemStack> getRecipeStacks() {
        return recipeStacks;
    }

    public ImmutableList<TagKey<Item>> getRecipeItemTags() {
        return recipeItemTags;
    }

    public int getManaCost() {
        return manaCost;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AgglomerationRecipe other)) return false;

        if (other.manaCost != manaCost) return false;
        if (!other.multiblockCenter.is(multiblockCenter.getBlock())) return false;
        if (!other.multiblockEdge.is(multiblockEdge.getBlock())) return false;
        if (!other.multiblockCorner.is(multiblockCorner.getBlock())) return false;

        if (!Objects.equals(other.multiblockCenterReplace, multiblockCenterReplace)) return false;
        if (!Objects.equals(other.multiblockEdgeReplace, multiblockEdgeReplace)) return false;
        if (!Objects.equals(other.multiblockCornerReplace, multiblockCornerReplace)) return false;

        // 比较输出列表
        if (other.recipeOutputs.size() != this.recipeOutputs.size()) return false;
        for (int i = 0; i < this.recipeOutputs.size(); i++) {
            if (!ItemStack.matches(this.recipeOutputs.get(i), other.recipeOutputs.get(i))) {
                return false;
            }
        }
        if (!new HashSet<>(other.recipeItemTags).equals(new HashSet<>(recipeItemTags))) return false;

        List<ItemStack> myStackCopy = new ArrayList<>(recipeStacks);
        for (ItemStack otherStack : other.recipeStacks) {
            myStackCopy.removeIf(stack -> ItemStack.matches(stack, otherStack));
        }
        return myStackCopy.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(manaCost, multiblockCenter.getBlock(), multiblockEdge.getBlock(),
                multiblockCorner.getBlock(), recipeOutputs);
    }

    @Override
    public String toString() {
        return "AgglomerationRecipe{" +
                "recipeStacks=" + recipeStacks +
                ", recipeItemTags=" + recipeItemTags +
                ", recipeOutputs=" + recipeOutputs +
                ", manaCost=" + manaCost +
                ", multiblockCenter=" + multiblockCenter +
                ", multiblockEdge=" + multiblockEdge +
                ", multiblockCorner=" + multiblockCorner +
                ", multiblockCenterReplace=" + multiblockCenterReplace +
                ", multiblockEdgeReplace=" + multiblockEdgeReplace +
                ", multiblockCornerReplace=" + multiblockCornerReplace +
                ", totalInputs=" + totalInputs +
                '}';
    }

    // NBT 子集检查（用于可能的高级匹配）
    private static boolean isTagSubset(@Nullable CompoundTag recipeTag, @Nullable CompoundTag suppliedTag) {
        if (recipeTag == null || recipeTag.isEmpty()) return true;
        if (suppliedTag == null || suppliedTag.isEmpty()) return false;
        if (recipeTag.size() > suppliedTag.size()) return false;

        for (String key : recipeTag.getAllKeys()) {
            if (!suppliedTag.contains(key)) return false;

            Tag recipeEntry = recipeTag.get(key);
            Tag suppliedEntry = suppliedTag.get(key);

            if (recipeEntry instanceof CompoundTag && suppliedEntry instanceof CompoundTag) {
                if (!isTagSubset((CompoundTag) recipeEntry, (CompoundTag) suppliedEntry)) return false;
            } else if (!recipeEntry.equals(suppliedEntry)) {
                return false;
            }
        }
        return true;
    }

}