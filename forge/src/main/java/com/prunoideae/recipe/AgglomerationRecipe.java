package com.prunoideae.recipe;

import com.google.common.collect.ImmutableList;
import com.prunoideae.KubeJSBotania;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;

import java.util.*;

public class AgglomerationRecipe implements Recipe<Container>, TerrestrialAgglomerationRecipe {
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

    private final ResourceLocation id;

    final int totalInputs;

    public AgglomerationRecipe(ImmutableList<Object> recipeInputs,
                               ImmutableList<ItemStack> recipeOutputs,
                               int manaCost,
                               BlockState multiblockCenter,
                               BlockState multiblockEdge,
                               BlockState multiblockCorner,
                               @Nullable BlockState multiblockCenterReplace,
                               @Nullable BlockState multiblockEdgeReplace,
                               @Nullable BlockState multiblockCornerReplace,
                               ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "Recipe ID must not be null");
        verifyInputs(recipeInputs);

        // 处理输入 - 注意：不再强制设置 count = 1，保留原始数量
        ImmutableList.Builder<ItemStack> stackInputBuilder = ImmutableList.builder();
        ImmutableList.Builder<TagKey<Item>> tagInputBuilder = ImmutableList.builder();
        for (Object o : recipeInputs) {
            if (o instanceof ItemStack) {
                ItemStack stack = ((ItemStack) o).copy();
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Empty ItemStack in recipe inputs");
                }
                // 保留原始数量，不设置 count = 1
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
        // 创建输入列表的副本
        List<ItemStack> inputs = new ArrayList<>();
        for (ItemStack stack : flattenedInputs) {
            inputs.add(stack.copy());
        }

        // 匹配具体物品（带数量）
        for (ItemStack recipeStack : recipeStacks) {
            int requiredCount = recipeStack.getCount();

            boolean found = false;
            for (int i = 0; i < inputs.size(); i++) {
                ItemStack input = inputs.get(i);
                if (ItemStack.isSameItemSameTags(recipeStack, input)) {
                    int availableCount = input.getCount();
                    if (availableCount >= requiredCount) {
                        // 消耗所需数量
                        input.shrink(requiredCount);
                        if (input.isEmpty()) {
                            inputs.remove(i);
                        }
                        found = true;
                        break;
                    } else {
                        // 数量不足
                        return false;
                    }
                }
            }
            if (!found) return false;
        }

        // 匹配标签
        for (TagKey<Item> tag : recipeItemTags) {
            boolean found = false;
            for (int i = 0; i < inputs.size(); i++) {
                ItemStack input = inputs.get(i);
                if (input.is(tag)) {
                    input.shrink(1);
                    if (input.isEmpty()) {
                        inputs.remove(i);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        // 检查是否还有多余物品（精确匹配）
        return inputs.stream().allMatch(ItemStack::isEmpty);
    }

    private ItemStack getItemStackFromBlockState(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.WATER) {
            return new ItemStack(Items.WATER_BUCKET);
        } else if (block == Blocks.LAVA) {
            return new ItemStack(Items.LAVA_BUCKET);
        } else {
            return new ItemStack(block);
        }
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

        return manaCost == other.manaCost &&
                Objects.equals(multiblockCenter, other.multiblockCenter) &&
                Objects.equals(multiblockEdge, other.multiblockEdge) &&
                Objects.equals(multiblockCorner, other.multiblockCorner) &&
                Objects.equals(multiblockCenterReplace, other.multiblockCenterReplace) &&
                Objects.equals(multiblockEdgeReplace, other.multiblockEdgeReplace) &&
                Objects.equals(multiblockCornerReplace, other.multiblockCornerReplace) &&
                Objects.equals(recipeOutputs, other.recipeOutputs) &&
                Objects.equals(recipeItemTags, other.recipeItemTags) &&
                Objects.equals(recipeStacks, other.recipeStacks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                manaCost,
                multiblockCenter,
                multiblockEdge,
                multiblockCorner,
                multiblockCenterReplace,
                multiblockEdgeReplace,
                multiblockCornerReplace,
                recipeOutputs
        );
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

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();

        // 添加物品输入
        for (ItemStack stack : recipeStacks) {
            list.add(Ingredient.of(stack));
        }
        for (TagKey<Item> tag : recipeItemTags) {
            list.add(Ingredient.of(tag));
        }

        // 添加结构方块作为输入材料
        addStructureBlocksToIngredients(list);

        if (list.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("getIngredients() returned null element");
        }
        return list;
    }

    /**
     * 将结构方块添加到输入材料列表中
     * 这样JEI就能识别这些方块作为配方的输入材料
     */
    private void addStructureBlocksToIngredients(NonNullList<Ingredient> list) {
        // 中心方块
        if (multiblockCenter != null) {
            ItemStack centerStack = getItemStackFromBlockState(multiblockCenter);
            if (!centerStack.isEmpty()) {
                list.add(Ingredient.of(centerStack));
            }
        }

        // 边缘方块
        if (multiblockEdge != null) {
            ItemStack edgeStack = getItemStackFromBlockState(multiblockEdge);
            if (!edgeStack.isEmpty()) {
                list.add(Ingredient.of(edgeStack));
            }
        }

        // 角落方块
        if (multiblockCorner != null) {
            ItemStack cornerStack = getItemStackFromBlockState(multiblockCorner);
            if (!cornerStack.isEmpty()) {
                list.add(Ingredient.of(cornerStack));
            }
        }
    }

    @Override
    public boolean matches(Container container, Level level) {
        // 实际匹配由 Mixin 处理，这里可以简单返回 true
        // 如果希望 JEI 能通过原料过滤配方，可以实现简单的物品匹配
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) inputs.add(stack);
        }
        return itemsMatch(inputs); // 忽略结构
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return getPrimaryOutputCopy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return getPrimaryOutputCopy();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AgglomerationRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return KubeJSBotania.CUSTOM_TERRA_PLATE_TYPE;
    }

    // Botania 接口的方法
    @Override
    public int getMana() {
        return manaCost;
    }
}