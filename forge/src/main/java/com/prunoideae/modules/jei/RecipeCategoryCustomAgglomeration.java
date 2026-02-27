package com.prunoideae.modules.jei;

import com.prunoideae.recipe.AgglomerationRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.ArrayList;
import java.util.List;

public class RecipeCategoryCustomAgglomeration implements IRecipeCategory<AgglomerationRecipe> {

    public static final RecipeType<AgglomerationRecipe> RECIPE_TYPE =
            RecipeType.create("botaniatweaks", "agglomeration", AgglomerationRecipe.class);

    private static final int WIDTH = 200;
    private static final int HEIGHT = 180;
    private static final int ITEM_SIZE = 16;
    private static final int SLOT_SPACING = 4;

    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;

    public RecipeCategoryCustomAgglomeration(IGuiHelper guiHelper) {
        this.title = Component.translatable("泰拉凝聚板");
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(BotaniaBlocks.terraPlate));
    }

    @Override
    public RecipeType<AgglomerationRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AgglomerationRecipe recipe, IFocusGroup focuses) {
        // ========== 1. 输入物品（水平单行居中） ==========
        List<List<ItemStack>> inputLists = new ArrayList<>();
        for (ItemStack stack : recipe.getRecipeStacks()) {
            inputLists.add(List.of(stack));
        }
        for (TagKey<Item> tag : recipe.getRecipeItemTags()) {
            List<ItemStack> tagStacks = ForgeRegistries.ITEMS.getValues().stream()
                    .filter(item -> item.builtInRegistryHolder().is(tag))
                    .map(ItemStack::new)
                    .toList();
            if (!tagStacks.isEmpty()) {
                inputLists.add(tagStacks);
            }
        }

        int inputCount = inputLists.size();
        int totalWidth = inputCount * ITEM_SIZE + (inputCount - 1) * SLOT_SPACING;
        int startX = (WIDTH - totalWidth) / 2;
        int startY = 10;

        for (int i = 0; i < inputCount; i++) {
            int x = startX + i * (ITEM_SIZE + SLOT_SPACING);
            builder.addSlot(RecipeIngredientRole.INPUT, x, startY)
                    .addItemStacks(inputLists.get(i));
        }

        // ========== 2. 输出物品（居中） ==========
        List<ItemStack> outputs = recipe.getOutputCopies();
        int outputCount = outputs.size();
        int outputTotalWidth = outputCount * ITEM_SIZE + (outputCount - 1) * SLOT_SPACING;
        int outputStartX = (WIDTH - outputTotalWidth) / 2;
        int outputY = startY + ITEM_SIZE + SLOT_SPACING + 10;

        for (int i = 0; i < outputCount; i++) {
            int x = outputStartX + i * (ITEM_SIZE + SLOT_SPACING);
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, outputY)
                    .addItemStack(outputs.get(i));
        }

        // ========== 3. 所需结构（左侧） ==========
        int gridStartY = outputY + ITEM_SIZE + 20;
        drawStructureGrid(builder, recipe, 30, gridStartY, false); // false = 使用原方块

        // ========== 4. 替换结构（右侧，如果有替换方块） ==========
        boolean hasReplacement = recipe.multiblockCenterReplace != null ||
                recipe.multiblockEdgeReplace != null ||
                recipe.multiblockCornerReplace != null;
        if (hasReplacement) {
            int rightGridX = WIDTH - 30 - 3 * (ITEM_SIZE + SLOT_SPACING);
            drawStructureGrid(builder, recipe, rightGridX, gridStartY, true); // true = 使用替换方块
        }
    }

    /**
     * 绘制3×3结构网格（所需结构或替换后结构）
     */
    private void drawStructureGrid(IRecipeLayoutBuilder builder, AgglomerationRecipe recipe,
                                   int gridX, int gridY, boolean useReplacement) {
        int cellSize = ITEM_SIZE + SLOT_SPACING;

        // 中心方块
        BlockState center = useReplacement ? recipe.multiblockCenterReplace : recipe.multiblockCenter;
        if (center != null) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, gridX + cellSize, gridY + cellSize)
                    .addItemStack(getDisplayStack(center))
                    .setSlotName(useReplacement ? "replace_center" : "center");
        }

        // 边缘方块（上下左右）
        BlockState edge = useReplacement ? recipe.multiblockEdgeReplace : recipe.multiblockEdge;
        if (edge != null) {
            ItemStack edgeStack = getDisplayStack(edge);
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, gridX + cellSize, gridY)
                    .addItemStack(edgeStack).setSlotName(useReplacement ? "replace_edge_top" : "edge_top");
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, gridX + cellSize, gridY + 2 * cellSize)
                    .addItemStack(edgeStack).setSlotName(useReplacement ? "replace_edge_bottom" : "edge_bottom");
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, gridX, gridY + cellSize)
                    .addItemStack(edgeStack).setSlotName(useReplacement ? "replace_edge_left" : "edge_left");
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, gridX + 2 * cellSize, gridY + cellSize)
                    .addItemStack(edgeStack).setSlotName(useReplacement ? "replace_edge_right" : "edge_right");
        }

        // 角落方块（四角）
        BlockState corner = useReplacement ? recipe.multiblockCornerReplace : recipe.multiblockCorner;
        if (corner != null) {
            ItemStack cornerStack = getDisplayStack(corner);
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, gridX, gridY)
                    .addItemStack(cornerStack).setSlotName(useReplacement ? "replace_corner_tl" : "corner_tl");
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, gridX + 2 * cellSize, gridY)
                    .addItemStack(cornerStack).setSlotName(useReplacement ? "replace_corner_tr" : "corner_tr");
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, gridX, gridY + 2 * cellSize)
                    .addItemStack(cornerStack).setSlotName(useReplacement ? "replace_corner_bl" : "corner_bl");
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, gridX + 2 * cellSize, gridY + 2 * cellSize)
                    .addItemStack(cornerStack).setSlotName(useReplacement ? "replace_corner_br" : "corner_br");
        }
    }

    /**
     * 辅助方法：将方块状态转换为适合 JEI 显示的物品（水/熔岩 → 桶，其他方块 → 对应物品）
     */
    private ItemStack getDisplayStack(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.WATER) {
            ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
            waterBucket.setHoverName(Component.literal("水源方块"));
            return waterBucket;
        } else if (block == Blocks.LAVA) {
            ItemStack lavaBucket = new ItemStack(Items.LAVA_BUCKET);
            lavaBucket.setHoverName(Component.literal("熔岩源方块"));
            return lavaBucket;
        } else {
            return new ItemStack(block);
        }
    }

    @Override
    public void draw(AgglomerationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int manaCost = recipe.manaCost;
        int manaBarWidth = 103;          // 魔力条宽度（Botania 原版）
        int manaBarHeight = 10;          // 魔力条高度
        int manaBarX = (WIDTH - manaBarWidth) / 2;   // 水平居中
        int manaBarY = HEIGHT - 30;       // 魔力条 Y 坐标（底部留空）

        // 绘制魔力条（自动处理零头进度）
        HUDHandler.renderManaBar(guiGraphics, manaBarX, manaBarY, 0x4444FF, 1.0F, manaCost, 1_000_000);

        // 计算需要的整池数
        int poolsNeeded = (int) Math.ceil((double) manaCost / 1_000_000.0);
        if (poolsNeeded > 1) {
            String poolText = "x" + poolsNeeded;
            int fontHeight = Minecraft.getInstance().font.lineHeight;
            // 计算倍数文字的 Y 坐标：使其与魔力条垂直居中
            int textY = manaBarY + (manaBarHeight - fontHeight) / 2;
            // 倍数文字显示在魔力条右侧，间距 5 像素
            guiGraphics.drawString(Minecraft.getInstance().font, poolText,
                    manaBarX + manaBarWidth + 5, textY, 0xAAAAAA, false);
        }

        // ========== 其他绘制（箭头）保持不变 ==========
        // 向下箭头
        int arrowDownX = (WIDTH - 16) / 2;
        int arrowDownY = 10 + ITEM_SIZE + 2;
        guiGraphics.drawString(Minecraft.getInstance().font, "↓", arrowDownX + 4, arrowDownY + 4, 0xFFFFFF, false);

        // 向右箭头（如果有替换结构）
        boolean hasReplacement = recipe.multiblockCenterReplace != null ||
                recipe.multiblockEdgeReplace != null ||
                recipe.multiblockCornerReplace != null;
        if (hasReplacement) {
            int arrowRightX = WIDTH / 2 - 8;
            int arrowRightY = HEIGHT - 80;
            guiGraphics.drawString(Minecraft.getInstance().font, "→", arrowRightX + 4, arrowRightY + 4, 0xFFFFFF, false);
        }
    }
}