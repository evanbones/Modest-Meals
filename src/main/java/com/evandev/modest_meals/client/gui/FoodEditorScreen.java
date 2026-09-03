package com.evandev.modest_meals.client.gui;

import com.evandev.modest_meals.client.gui.util.GuiUtil;
import com.evandev.modest_meals.client.gui.widget.DropdownWidget;
import com.evandev.modest_meals.client.gui.widget.ModButton;
import com.evandev.modest_meals.client.gui.widget.ModEditBox;
import com.evandev.modest_meals.food.CustomFoodDatapack;
import com.evandev.modest_meals.food.FoodProfile;
import com.evandev.modest_meals.food.FoodProfileManager;
import com.evandev.modest_meals.food.FoodValues;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FoodEditorScreen extends Screen {
    private static final int HEADER_H = 28;
    private static final int FOOTER_H = 32;
    private static final int GUTTER = 6;
    private static final int ROW_H = 20;
    private static final int SEARCH_H = 18;
    private static final int TRAIT_ROW_H = 20;
    private static final int BUTTON_H = 18;

    private final Screen parent;
    private final Map<String, List<FoodTrait>> customTraits;
    private final List<FoodProfile> customProfiles;

    private final List<Candidate> candidates = new ArrayList<>();

    private ModEditBox searchBox;
    private DropdownWidget filterDropdown;
    private FoodItemList itemList;

    private Item selectedItem;
    private FilterMode filterMode = FilterMode.FOODS_ONLY;

    private Component statusMessage = null;
    private int statusMessageTicks = 0;

    private String pendingSearch = "";
    private double pendingListScroll = 0;

    private double traitScroll = 0;
    private boolean draggingTraitScrollbar = false;

    private int leftX, leftW, rightX, rightW, contentTop, contentBottom;

    public FoodEditorScreen(Screen parent) {
        super(Component.translatable("gui.modest_meals.food_editor.title"));
        this.parent = parent;
        this.customTraits = new LinkedHashMap<>(CustomFoodDatapack.readCustomTraits());
        this.customProfiles = new ArrayList<>(CustomFoodDatapack.readCustomProfiles());

        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            candidates.add(new Candidate(item, id,
                    stack.getHoverName().getString().toLowerCase(Locale.ROOT),
                    id.toLowerCase(Locale.ROOT),
                    FoodValues.isFood(stack)));
        }
    }

    private static String itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static String fmt(float v) {
        return v == (long) v ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.2f", v);
    }

    @Override
    protected void init() {
        super.init();

        if (this.searchBox != null) this.pendingSearch = this.searchBox.getValue();
        if (this.itemList != null) this.pendingListScroll = this.itemList.getScrollAmount();

        this.leftX = GUTTER + GuiUtil.PANEL_PADDING;
        this.leftW = Mth.clamp((int) (this.width * 0.30), 150, 240);
        this.rightX = leftX + leftW + GuiUtil.PANEL_PADDING + GUTTER + GuiUtil.PANEL_PADDING;
        this.rightW = Math.max(120, this.width - rightX - GuiUtil.PANEL_PADDING - GUTTER);
        this.contentTop = HEADER_H + GuiUtil.PANEL_PADDING;
        this.contentBottom = this.height - FOOTER_H - GuiUtil.PANEL_PADDING;

        layoutWidgets();
    }

    private int searchY() {
        return HEADER_H + 8;
    }

    private int filterY() {
        return searchY() + SEARCH_H + 4;
    }

    private int listTop() {
        return filterY() + BUTTON_H + GuiUtil.PANEL_PADDING + 4;
    }

    private void layoutWidgets() {
        this.clearWidgets();

        this.searchBox = new ModEditBox(this.font, leftX, searchY(), leftW, SEARCH_H,
                Component.translatable("gui.modest_meals.search"));
        this.searchBox.setPlaceholder(Component.translatable("gui.modest_meals.search"));
        this.searchBox.setValue(this.pendingSearch);
        this.searchBox.setResponder(s -> {
            this.pendingSearch = s;
            refreshItemList();
        });
        this.addRenderableWidget(this.searchBox);

        List<String> filterIds = new ArrayList<>();
        for (FilterMode m : FilterMode.values()) filterIds.add(m.name());
        this.filterDropdown = new DropdownWidget(leftX, filterY(), leftW, BUTTON_H,
                Component.empty(), filterIds, id -> FilterMode.valueOf(id).getTitle(), id -> {
            this.filterMode = FilterMode.valueOf(id);
            refreshItemList();
        });
        this.filterDropdown.setSelectedValue(filterMode.name());
        this.addRenderableWidget(this.filterDropdown);

        this.itemList = new FoodItemList(this.minecraft, leftW - GuiUtil.SCROLLBAR_EXTRA_WIDTH,
                contentBottom - listTop(), listTop(), ROW_H);
        this.itemList.setX(leftX);
        this.addRenderableWidget(this.itemList);

        refreshItemList();
        this.itemList.setScrollAmount(this.pendingListScroll);

        int footerY = this.height - FOOTER_H + 6;
        this.addRenderableWidget(new ModButton(GUTTER, footerY, 70, 20, CommonComponents.GUI_DONE,
                b -> this.minecraft.setScreen(this.parent)));

        int saveW = 110;
        int saveX = this.width - GUTTER - saveW;
        this.addRenderableWidget(new ModButton(saveX, footerY, saveW, 20,
                Component.translatable("gui.modest_meals.save_and_apply"), b -> saveAndApply()));

        if (selectedItem != null && isCustomized(itemId(selectedItem))) {
            int resetW = 90;
            this.addRenderableWidget(new ModButton(saveX - GUTTER - resetW, footerY, resetW, 20,
                    Component.translatable("gui.modest_meals.reset_item"), b -> resetSelectedItem()));
        }

        if (selectedItem != null) {
            rebuildRightPane();
        }
    }

    private int sectionY(int preferredY) {
        return Mth.clamp(preferredY, contentTop, Math.max(contentTop, contentBottom - BUTTON_H));
    }

    private int scalingHeaderY() {
        return sectionY(contentTop + 46);
    }

    private int traitsHeaderY() {
        return sectionY(scalingHeaderY() + 38);
    }

    private int traitsTop() {
        return traitsHeaderY() + 14;
    }

    private int traitsViewHeight() {
        return Math.max(0, contentBottom - traitsTop());
    }

    private int traitRowWidth() {
        return rightW - GuiUtil.SCROLLBAR_EXTRA_WIDTH;
    }

    private void rebuildRightPane() {
        String itemId = itemId(selectedItem);

        int editW = Math.min(90, Math.max(50, rightW / 3));
        this.addRenderableWidget(new ModButton(rightX + rightW - editW, scalingHeaderY() - 4, editW, BUTTON_H,
                Component.translatable("gui.modest_meals.edit_profile"), b -> {
            FoodProfile prof = getResolvedProfile(selectedItem);
            this.minecraft.setScreen(new ProfileEditDialog(this, selectedItem, prof, newProf -> {
                customProfiles.removeIf(p -> p.match().equals(itemId) || p.id().equals(itemId + "_override"));
                customProfiles.add(newProf);
                layoutWidgets();
                setStatus("gui.modest_meals.profile_updated");
            }));
        }));

        int addW = Math.min(90, Math.max(50, rightW / 3));
        this.addRenderableWidget(new ModButton(rightX + rightW - addW, traitsHeaderY() - 4, addW, BUTTON_H,
                Component.translatable("gui.modest_meals.add_trait"), b ->
                this.minecraft.setScreen(new TraitEditDialog(this, newTrait -> {
                    traitsFor(itemId).add(newTrait);
                    this.traitScroll = 0;
                    layoutWidgets();
                    setStatus("gui.modest_meals.trait_added");
                }, null))));

        List<FoodTrait> traits = getEffectiveTraits(selectedItem);
        this.traitScroll = Mth.clamp(this.traitScroll, 0, maxTraitScroll(traits.size()));

        int viewTop = traitsTop();
        int viewBottom = viewTop + traitsViewHeight();
        int delW = 18;
        int editBtnW = 34;
        int delX = rightX + traitRowWidth() - delW - 2;
        int editBtnX = delX - editBtnW - 3;

        for (int i = 0; i < traits.size(); i++) {
            final int traitIndex = i;
            final FoodTrait trait = traits.get(i);

            int rowY = viewTop + i * TRAIT_ROW_H - (int) traitScroll;
            if (rowY + TRAIT_ROW_H <= viewTop || rowY >= viewBottom) continue;

            Button edit = new ModButton(editBtnX, rowY + 1, editBtnW, 16,
                    Component.translatable("gui.modest_meals.edit"), b ->
                    this.minecraft.setScreen(new TraitEditDialog(this, editedTrait -> {
                        List<FoodTrait> list = traitsFor(itemId);
                        if (traitIndex < list.size()) {
                            list.set(traitIndex, editedTrait);
                        } else {
                            list.add(editedTrait);
                        }
                        layoutWidgets();
                        setStatus("gui.modest_meals.trait_updated");
                    }, trait)));

            Button delete = new ModButton(delX, rowY + 1, delW, 16,
                    Component.literal("x").withStyle(ChatFormatting.RED), b -> {
                List<FoodTrait> list = traitsFor(itemId);
                if (traitIndex < list.size()) list.remove(traitIndex);
                layoutWidgets();
                setStatus("gui.modest_meals.trait_removed");
            });

            this.addRenderableWidget(edit);
            this.addRenderableWidget(delete);
        }
    }

    private List<FoodTrait> traitsFor(String itemId) {
        return customTraits.computeIfAbsent(itemId,
                k -> new ArrayList<>(FoodTraitManager.getTraits(selectedItem)));
    }

    private int maxTraitScroll(int traitCount) {
        return Math.max(0, traitCount * TRAIT_ROW_H - traitsViewHeight());
    }

    private void refreshItemList() {
        if (this.itemList == null) return;

        this.itemList.clearEntries();
        String query = this.pendingSearch == null ? "" : this.pendingSearch.toLowerCase(Locale.ROOT).trim();

        for (Candidate c : candidates) {
            if (!query.isEmpty() && !c.lowerName().contains(query) && !c.lowerId().contains(query)) continue;
            if (filterMode == FilterMode.FOODS_ONLY && !c.isFood()) continue;
            if (filterMode == FilterMode.CUSTOMIZED_ONLY && !isCustomized(c.id())) continue;

            FoodItemList.ItemEntry entry = new FoodItemList.ItemEntry(c.item(), this);
            this.itemList.addEntry(entry);
            if (c.item() == selectedItem) this.itemList.setSelected(entry);
        }
    }

    private boolean isCustomized(String itemId) {
        return customTraits.containsKey(itemId) || hasCustomProfile(itemId);
    }

    private boolean hasCustomProfile(String itemId) {
        for (FoodProfile p : customProfiles) {
            if (p.match().equals(itemId) || p.id().equals(itemId + "_override")) return true;
        }
        return false;
    }

    private FoodProfile getResolvedProfile(Item item) {
        String id = itemId(item);
        for (FoodProfile p : customProfiles) {
            if (p.match().equals(id) || p.id().equals(id + "_override")) return p;
        }
        return FoodProfileManager.resolve(new ItemStack(item))
                .orElse(new FoodProfile("default", "*", 0, 1.0f, 30.0f, 0.0f));
    }

    private List<FoodTrait> getEffectiveTraits(Item item) {
        String id = itemId(item);
        if (customTraits.containsKey(id)) return customTraits.get(id);
        return FoodTraitManager.getTraits(item);
    }

    public void selectItem(Item item) {
        this.selectedItem = item;
        this.traitScroll = 0;
        if (this.itemList != null) this.pendingListScroll = this.itemList.getScrollAmount();
        layoutWidgets();
    }

    private void resetSelectedItem() {
        if (selectedItem == null) return;
        String id = itemId(selectedItem);
        customTraits.remove(id);
        customProfiles.removeIf(p -> p.match().equals(id) || p.id().equals(id + "_override"));
        layoutWidgets();
        setStatus("gui.modest_meals.item_reset");
    }

    private void saveAndApply() {
        CustomFoodDatapack.saveCustomTraits(customTraits);
        CustomFoodDatapack.saveCustomProfiles(customProfiles);

        for (Map.Entry<String, List<FoodTrait>> entry : customTraits.entrySet()) {
            if (!entry.getKey().startsWith("#")) {
                FoodTraitManager.setItemTraits(ResourceLocation.parse(entry.getKey()), entry.getValue());
            }
        }
        for (FoodProfile p : customProfiles) {
            FoodProfileManager.upsertProfile(p);
        }

        setStatus("gui.modest_meals.saved_and_applied");
    }

    private void setStatus(String translationKey) {
        this.statusMessage = Component.translatable(translationKey).withStyle(ChatFormatting.GREEN);
        this.statusMessageTicks = 60;
    }

    @Override
    public void tick() {
        super.tick();
        if (statusMessageTicks > 0) statusMessageTicks--;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (filterDropdown != null && filterDropdown.isOpen()
                && filterDropdown.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(filterDropdown);
            return true;
        }
        if (button == 0 && isOverTraitScrollbar(mouseX, mouseY)) {
            this.draggingTraitScrollbar = true;
            updateTraitScroll(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int currentMaxTraitScroll() {
        return selectedItem == null ? 0 : maxTraitScroll(getEffectiveTraits(selectedItem).size());
    }

    private boolean isOverTraitScrollbar(double mouseX, double mouseY) {
        if (currentMaxTraitScroll() <= 0) return false;
        int barX = rightX + traitRowWidth();
        return mouseX >= barX && mouseX < barX + GuiUtil.SCROLLBAR_WIDTH
                && mouseY >= traitsTop() && mouseY < traitsTop() + traitsViewHeight();
    }

    private void updateTraitScroll(double mouseY) {
        this.traitScroll = GuiUtil.scrollAmountFromMouse(mouseY, traitsTop(), traitsViewHeight(),
                currentMaxTraitScroll());
        layoutWidgets();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingTraitScrollbar) {
            updateTraitScroll(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingTraitScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (filterDropdown != null && filterDropdown.isOpen()
                && filterDropdown.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        int max = currentMaxTraitScroll();
        if (max > 0 && mouseX >= rightX && mouseX < rightX + rightW
                && mouseY >= traitsTop() && mouseY < traitsTop() + traitsViewHeight()) {
            this.traitScroll = Mth.clamp(this.traitScroll - scrollY * TRAIT_ROW_H, 0, max);
            layoutWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (filterDropdown != null && filterDropdown.isOpen()
                && filterDropdown.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(0, 0, this.width, HEADER_H, GuiUtil.HEADER_BG);
        graphics.renderOutline(0, 0, this.width, HEADER_H, GuiUtil.HEADER_OUTLINE);

        GuiUtil.drawContentPanel(graphics, rightX, contentTop, rightW, contentBottom - contentTop);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        boolean showStatus = statusMessage != null && statusMessageTicks > 0;
        String status = showStatus ? GuiUtil.trim(this.font, statusMessage.getString(), this.width / 2 - 12) : "";
        int statusW = status.isEmpty() ? 0 : this.font.width(status) + 8;

        GuiUtil.drawTrimmed(graphics, this.font, this.title, 8, 10, this.width - 16 - statusW, GuiUtil.WHITE);
        if (!status.isEmpty()) {
            graphics.drawString(this.font, status, this.width - 8 - this.font.width(status), 10, GuiUtil.STATUS_GREEN);
        }

        if (selectedItem != null) {
            renderDetailPane(graphics);
        } else {
            GuiUtil.drawWrappedCentered(graphics, this.font, Component.translatable("gui.modest_meals.no_item_selected"),
                    rightX + rightW / 2, contentTop + (contentBottom - contentTop) / 2, rightW,
                    (contentBottom - contentTop) / GuiUtil.LINE_H, GuiUtil.EMPTY_STATE);
        }

        if (filterDropdown != null) filterDropdown.renderOverlay(graphics, mouseX, mouseY);
    }

    private void renderDetailPane(GuiGraphics graphics) {
        ItemStack stack = new ItemStack(selectedItem);
        String id = itemId(selectedItem);

        GuiUtil.drawSlot(graphics, rightX, contentTop);
        graphics.renderFakeItem(stack, rightX + 1, contentTop + 1);
        graphics.renderItemDecorations(this.font, stack, rightX + 1, contentTop + 1);

        int textX = rightX + 24;
        int textW = rightW - 24;
        GuiUtil.drawTrimmed(graphics, this.font, stack.getHoverName(), textX, contentTop + 1, textW, GuiUtil.WHITE);
        GuiUtil.drawTrimmed(graphics, this.font, Component.literal(id), textX, contentTop + 11, textW, GuiUtil.DIM);

        int nutrition = FoodValues.nutritionOf(stack);
        boolean isFood = FoodValues.isFood(stack);
        Component nutrText = isFood
                ? Component.translatable("gui.modest_meals.nutrition_value", nutrition).withStyle(ChatFormatting.GOLD)
                : Component.translatable("gui.modest_meals.not_food").withStyle(ChatFormatting.GRAY);
        GuiUtil.drawTrimmed(graphics, this.font, nutrText, rightX, contentTop + 26, rightW, GuiUtil.WHITE);

        int scalingY = scalingHeaderY();
        GuiUtil.drawSeparator(graphics, rightX, scalingY - 8, rightW);
        int editW = Math.min(90, Math.max(50, rightW / 3));
        GuiUtil.drawTrimmed(graphics, this.font,
                Component.translatable("gui.modest_meals.food_profile_header").withStyle(ChatFormatting.YELLOW),
                rightX, scalingY, rightW - editW - 6, 0xFFFF55);

        FoodProfile profile = getResolvedProfile(selectedItem);
        Component profInfo = Component.translatable("gui.modest_meals.profile_summary",
                fmt(profile.healthPerNutrition()), fmt(profile.healthTicksPerPoint()), fmt(profile.staminaPerNutrition()));
        int summaryTop = scalingY + 16;
        GuiUtil.drawWrapped(graphics, this.font, profInfo, rightX, summaryTop, rightW,
                Math.max(1, (traitsHeaderY() - 8 - summaryTop) / GuiUtil.LINE_H), GuiUtil.LABEL);

        int traitsY = traitsHeaderY();
        GuiUtil.drawSeparator(graphics, rightX, traitsY - 8, rightW);
        int addW = Math.min(90, Math.max(50, rightW / 3));
        GuiUtil.drawTrimmed(graphics, this.font,
                Component.translatable("gui.modest_meals.traits_header").withStyle(ChatFormatting.YELLOW),
                rightX, traitsY, rightW - addW - 6, 0xFFFF55);

        renderTraitRows(graphics);
    }

    private void renderTraitRows(GuiGraphics graphics) {
        List<FoodTrait> traits = getEffectiveTraits(selectedItem);
        int viewTop = traitsTop();
        int viewH = traitsViewHeight();
        if (viewH <= 0) return;

        if (traits.isEmpty()) {
            GuiUtil.drawWrapped(graphics, this.font,
                    Component.translatable("gui.modest_meals.no_traits")
                            .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY),
                    rightX, viewTop + 4, rightW, Math.max(1, (viewH - 4) / GuiUtil.LINE_H), GuiUtil.DIM);
            return;
        }

        int textWidth = traitRowWidth() - 34 - 18 - 10;

        graphics.enableScissor(rightX, viewTop, rightX + rightW, viewTop + viewH);
        for (int i = 0; i < traits.size(); i++) {
            int rowY = viewTop + i * TRAIT_ROW_H - (int) traitScroll;
            if (rowY + TRAIT_ROW_H <= viewTop || rowY >= viewTop + viewH) continue;

            Component tooltip = traits.get(i).getTooltipComponent(1.0, 1.0);
            if (tooltip == null) continue;
            GuiUtil.drawTrimmed(graphics, this.font, tooltip, rightX, rowY + 5, textWidth, GuiUtil.WHITE);
        }
        graphics.disableScissor();

        int max = maxTraitScroll(traits.size());
        if (max > 0) {
            GuiUtil.drawVanillaScrollbar(graphics, rightX + traitRowWidth(), viewTop, viewH, traitScroll, max);
        }
    }

    public enum FilterMode {
        ALL("gui.modest_meals.filter.all"),
        FOODS_ONLY("gui.modest_meals.filter.foods"),
        CUSTOMIZED_ONLY("gui.modest_meals.filter.customized");

        private final String key;

        FilterMode(String key) {
            this.key = key;
        }

        public Component getTitle() {
            return Component.translatable(key);
        }
    }

    private record Candidate(Item item, String id, String lowerName, String lowerId, boolean isFood) {
    }

    public static class FoodItemList extends ObjectSelectionList<FoodItemList.ItemEntry> {
        private boolean draggingScrollbar = false;

        public FoodItemList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
            this.setRenderHeader(false, 0);
        }

        public int addEntry(ItemEntry entry) {
            return super.addEntry(entry);
        }

        public void clearEntries() {
            super.clearEntries();
        }

        @Override
        public int getRowWidth() {
            return this.width - GuiUtil.ROW_INSET * 2;
        }

        @Override
        public int getRowLeft() {
            return this.getX() + GuiUtil.ROW_INSET;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.width;
        }

        @Override
        protected boolean scrollbarVisible() {
            return false;
        }

        @Override
        protected void renderListSeparators(@NotNull GuiGraphics graphics) {
        }

        @Override
        protected void renderListBackground(@NotNull GuiGraphics graphics) {
            GuiUtil.drawContentPanel(graphics, this.getX(), this.getY(), this.width, this.height);
        }

        @Override
        protected void enableScissor(@NotNull GuiGraphics graphics) {
            graphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
        }

        @Override
        protected void renderSelection(@NotNull GuiGraphics graphics, int top, int width, int height, int outer, int inner) {
        }

        @Override
        protected void renderDecorations(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
            GuiUtil.drawVanillaScrollbar(graphics, getScrollbarPosition(), this.getY(), this.height,
                    this.getScrollAmount(), this.getMaxScroll());
        }

        private boolean isOverScrollbar(double mouseX, double mouseY) {
            return mouseX >= getScrollbarPosition() && mouseX < getScrollbarPosition() + GuiUtil.SCROLLBAR_WIDTH
                    && mouseY >= this.getY() && mouseY < this.getBottom();
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return super.isMouseOver(mouseX, mouseY) || isOverScrollbar(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && isOverScrollbar(mouseX, mouseY) && getMaxScroll() > 0) {
                this.draggingScrollbar = true;
                this.setScrollAmount(GuiUtil.scrollAmountFromMouse(mouseY, this.getY(), this.height, getMaxScroll()));
                return true;
            }
            this.draggingScrollbar = false;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (draggingScrollbar) {
                this.setScrollAmount(GuiUtil.scrollAmountFromMouse(mouseY, this.getY(), this.height, getMaxScroll()));
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            this.draggingScrollbar = false;
            return super.mouseReleased(mouseX, mouseY, button);
        }

        public static class ItemEntry extends ObjectSelectionList.Entry<ItemEntry> {
            public final Item item;
            private final FoodEditorScreen screen;

            public ItemEntry(Item item, FoodEditorScreen screen) {
                this.item = item;
                this.screen = screen;
            }

            @Override
            public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean isHovered, float partialTick) {
                boolean selected = screen.selectedItem == this.item;

                int rowH = height + 2;
                GuiUtil.drawRow(graphics, left, top, width, rowH, selected, isHovered);

                ItemStack stack = new ItemStack(item);
                graphics.renderFakeItem(stack, left + 3, top + (rowH - 16) / 2);

                String idStr = itemId(item);
                boolean isCustom = screen.isCustomized(idStr);

                int badgeW = isCustom ? 8 : 0;
                var font = Minecraft.getInstance().font;
                graphics.drawString(font,
                        GuiUtil.trim(font, stack.getHoverName().getString(), width - 24 - badgeW - 4),
                        left + 22, top + (rowH - 8) / 2, selected ? GuiUtil.WHITE : 0xE0E0E0);

                if (isCustom) {
                    graphics.drawString(font, "*", left + width - 8, top + (rowH - 8) / 2, GuiUtil.BADGE_CUSTOM);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                screen.selectItem(this.item);
                return true;
            }

            @Override
            public @NotNull Component getNarration() {
                return new ItemStack(item).getHoverName();
            }
        }
    }
}
