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
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
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

    private static final int PANE_PAD = 3;
    private static final int VY_NUTRITION = 26;
    private static final int VY_PROFILE_HEADER = 46;
    private static final int VY_PROFILE_SUMMARY = 62;
    private static final int PROFILE_SUMMARY_MAX_LINES = 4;
    private static final int EMPTY_TRAITS_H = 20;

    private final Screen parent;
    private final boolean readOnly;
    private final Map<String, List<FoodTrait>> customTraits;
    private final Map<String, Set<String>> customSuppressions;
    private final List<FoodProfile> customProfiles;

    private final List<Candidate> candidates = new ArrayList<>();
    private final List<AbstractWidget> paneWidgets = new ArrayList<>();
    private ModEditBox searchBox;
    private DropdownWidget filterDropdown;
    private FoodItemList itemList;
    private Item selectedItem;
    private FilterMode filterMode = FilterMode.FOODS_ONLY;
    private Component statusMessage = null;
    private int statusMessageTicks = 0;
    private boolean dirty = false;
    private String pendingSearch = "";
    private double pendingListScroll = 0;
    private double paneScroll = 0;
    private boolean draggingPaneScrollbar = false;
    private int leftX, leftW, rightX, rightW, contentTop, contentBottom;

    public FoodEditorScreen(Screen parent) {
        super(Component.translatable("gui.modest_meals.food_editor.title"));
        this.parent = parent;
        this.readOnly = isRemoteServer();
        this.customTraits = new LinkedHashMap<>();
        this.customSuppressions = new LinkedHashMap<>();
        this.customProfiles = new ArrayList<>();

        if (!readOnly) {
            this.customTraits.putAll(CustomFoodDatapack.readCustomTraits());
            CustomFoodDatapack.readCustomSuppressions().forEach((id, keys) ->
                    this.customSuppressions.put(id, new LinkedHashSet<>(keys)));
            this.customProfiles.addAll(CustomFoodDatapack.readCustomProfiles());
        }

        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            candidates.add(new Candidate(item, id,
                    stack.getHoverName().getString().toLowerCase(Locale.ROOT),
                    id.toLowerCase(Locale.ROOT),
                    FoodValues.isFood(stack)));
        }
    }

    private static boolean isRemoteServer() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null && !connection.getConnection().isMemoryConnection();
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
        if (this.itemList != null) this.pendingListScroll = this.itemList.getScrollAmount();
        this.clearWidgets();
        this.paneWidgets.clear();

        this.searchBox = new ModEditBox(this.font, leftX, searchY(), leftW, SEARCH_H,
                Component.translatable("gui.modest_meals.search"));
        this.searchBox.setPlaceholder(Component.translatable("gui.modest_meals.search"));
        this.searchBox.setValue(this.pendingSearch);
        this.searchBox.setResponder(s -> {
            this.pendingSearch = s;
            refreshItemList();
            this.itemList.setScrollAmount(0);
            this.pendingListScroll = 0;
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
                b -> this.onClose()));

        if (!readOnly) {
            int saveW = 110;
            int saveX = this.width - GUTTER - saveW;
            ModButton saveButton = new ModButton(saveX, footerY, saveW, 20,
                    Component.translatable("gui.modest_meals.save_and_apply"), b -> saveAndApply());
            saveButton.active = dirty;
            this.addRenderableWidget(saveButton);

            if (selectedItem != null && isCustomized(itemId(selectedItem))) {
                int resetW = 90;
                this.addRenderableWidget(new ModButton(saveX - GUTTER - resetW, footerY, resetW, 20,
                        Component.translatable("gui.modest_meals.reset_item"), b -> resetSelectedItem()));
            }
        }

        if (selectedItem != null) {
            rebuildRightPane();
        }
    }

    private int paneContentX() {
        return rightX + PANE_PAD;
    }

    private int paneContentWidth() {
        return Math.max(1, rightW - GuiUtil.SCROLLBAR_EXTRA_WIDTH - PANE_PAD * 2);
    }

    private int paneTop() {
        return contentTop + PANE_PAD;
    }

    private int paneViewHeight() {
        return Math.max(0, contentBottom - PANE_PAD - paneTop());
    }

    private int paneY(int virtualY) {
        return paneTop() + virtualY - (int) paneScroll;
    }

    private int scrollbarX() {
        return rightX + rightW - GuiUtil.SCROLLBAR_EXTRA_WIDTH;
    }

    private int scrollTrackHeight() {
        return Math.max(0, contentBottom - contentTop);
    }

    private Component profileSummary() {
        FoodProfile profile = getResolvedProfile(selectedItem);
        return Component.translatable("gui.modest_meals.profile_summary",
                fmt(profile.healthPerNutrition()), fmt(profile.healthTicksPerPoint()),
                fmt(profile.staminaPerNutrition()));
    }

    private int profileSummaryLines() {
        if (selectedItem == null) return 1;
        return Math.max(1, GuiUtil.wrap(this.font, profileSummary().getString(),
                paneContentWidth(), PROFILE_SUMMARY_MAX_LINES).size());
    }

    private int vyTraitsHeader() {
        return VY_PROFILE_SUMMARY + profileSummaryLines() * GuiUtil.LINE_H + 8;
    }

    private int vyTraitsTop() {
        return vyTraitsHeader() + 14;
    }

    private int paneContentHeight() {
        if (selectedItem == null) return 0;
        int rows = getEffectiveTraits(selectedItem).size();
        return vyTraitsTop() + (rows == 0 ? EMPTY_TRAITS_H : rows * TRAIT_ROW_H);
    }

    private int maxPaneScroll() {
        return Math.max(0, paneContentHeight() - paneViewHeight());
    }

    private void addPaneWidget(AbstractWidget widget) {
        if (widget.getY() + widget.getHeight() <= contentTop || widget.getY() >= contentBottom) return;
        this.paneWidgets.add(widget);
        this.addWidget(widget);
    }

    private void renderPaneWidgets(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int hoverY = mouseY >= contentTop && mouseY < contentBottom ? mouseY : -1;
        for (AbstractWidget widget : paneWidgets) {
            widget.render(graphics, mouseX, hoverY, partialTick);
        }
    }

    private void rebuildRightPane() {
        this.paneScroll = Mth.clamp(this.paneScroll, 0, maxPaneScroll());
        if (readOnly) return;

        String itemId = itemId(selectedItem);
        int contentW = paneContentWidth();

        int editW = Math.min(90, Math.max(50, contentW / 3));
        addPaneWidget(new PaneButton(paneContentX() + contentW - editW, paneY(VY_PROFILE_HEADER - 4), editW, BUTTON_H,
                Component.translatable("gui.modest_meals.edit_profile"), b -> {
            FoodProfile prof = getResolvedProfile(selectedItem);
            this.minecraft.setScreen(new ProfileEditDialog(this, selectedItem, prof, newProf -> {
                customProfiles.removeIf(p -> p.match().equals(itemId) || p.id().equals(itemId + "_override"));
                customProfiles.add(newProf);
                dirty = true;
                layoutWidgets();
                setStatus("gui.modest_meals.profile_updated");
            }));
        }));

        addPaneWidget(new PaneButton(paneContentX() + contentW - editW, paneY(vyTraitsHeader() - 4), editW, BUTTON_H,
                Component.translatable("gui.modest_meals.add_trait"), b ->
                this.minecraft.setScreen(new TraitEditDialog(this, newTrait -> {
                    putTrait(itemId, newTrait);
                    dirty = true;
                    this.paneScroll = 0;
                    layoutWidgets();
                    setStatus("gui.modest_meals.trait_added");
                }, null))));

        List<FoodTrait> traits = getEffectiveTraits(selectedItem);
        int delW = 18;
        int editBtnW = 34;
        int delX = paneContentX() + contentW - delW - 2;
        int editBtnX = delX - editBtnW - 3;
        int rowsTop = vyTraitsTop();

        for (int i = 0; i < traits.size(); i++) {
            final FoodTrait trait = traits.get(i);
            int rowY = paneY(rowsTop + i * TRAIT_ROW_H);

            addPaneWidget(new PaneButton(editBtnX, rowY + 1, editBtnW, 16,
                    Component.translatable("gui.modest_meals.edit"), b ->
                    this.minecraft.setScreen(new TraitEditDialog(this, editedTrait -> {
                        removeTrait(itemId, trait);
                        putTrait(itemId, editedTrait);
                        dirty = true;
                        layoutWidgets();
                        setStatus("gui.modest_meals.trait_updated");
                    }, trait))));

            addPaneWidget(new PaneButton(delX, rowY + 1, delW, 16,
                    Component.literal("x").withStyle(ChatFormatting.RED), b -> {
                removeTrait(itemId, trait);
                dirty = true;
                layoutWidgets();
                setStatus("gui.modest_meals.trait_removed");
            }));
        }
    }

    private List<FoodTrait> traitsFor(String itemId) {
        return customTraits.computeIfAbsent(itemId,
                k -> new ArrayList<>(FoodTraitManager.getBaselineItemTraits(selectedItem)));
    }

    private void putTrait(String itemId, FoodTrait trait) {
        String key = FoodTraitManager.getMergeKey(trait);
        List<FoodTrait> list = traitsFor(itemId);
        list.removeIf(t -> FoodTraitManager.getMergeKey(t).equals(key));
        list.add(trait);
        suppressionsFor(itemId).remove(key);
    }

    private void removeTrait(String itemId, FoodTrait trait) {
        String key = FoodTraitManager.getMergeKey(trait);
        traitsFor(itemId).removeIf(t -> FoodTraitManager.getMergeKey(t).equals(key));
        suppressionsFor(itemId).add(key);
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
        return customTraits.containsKey(itemId) || !suppressionsFor(itemId).isEmpty() || hasCustomProfile(itemId);
    }

    private Set<String> suppressionsFor(String itemId) {
        return customSuppressions.computeIfAbsent(itemId, k -> new LinkedHashSet<>());
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
        return FoodProfileManager.resolveBaseline(new ItemStack(item))
                .orElse(new FoodProfile("default", "*", 0, 1.0f, 30.0f, 0.0f));
    }

    private List<FoodTrait> getEffectiveTraits(Item item) {
        String id = itemId(item);
        Set<String> suppressed = suppressionsFor(id);

        Map<String, FoodTrait> merged = new LinkedHashMap<>();
        List<FoodTrait> own = customTraits.containsKey(id)
                ? customTraits.get(id)
                : FoodTraitManager.getBaselineItemTraits(item);
        for (FoodTrait trait : own) {
            merged.putIfAbsent(FoodTraitManager.getMergeKey(trait), trait);
        }
        for (FoodTrait trait : FoodTraitManager.getBaselineTagTraits(item)) {
            merged.putIfAbsent(FoodTraitManager.getMergeKey(trait), trait);
        }
        merged.keySet().removeAll(suppressed);

        return FoodValues.withDerived(new ItemStack(item), new ArrayList<>(merged.values()),
                getResolvedProfile(item), suppressed);
    }

    public void selectItem(Item item) {
        this.selectedItem = item;
        this.paneScroll = 0;
        if (this.itemList != null) this.pendingListScroll = this.itemList.getScrollAmount();
        layoutWidgets();
    }

    private void resetSelectedItem() {
        if (readOnly || selectedItem == null) return;
        String id = itemId(selectedItem);
        customTraits.remove(id);
        customSuppressions.remove(id);
        customProfiles.removeIf(p -> p.match().equals(id) || p.id().equals(id + "_override"));
        dirty = true;
        layoutWidgets();
        setStatus("gui.modest_meals.item_reset");
    }

    private void saveAndApply() {
        if (readOnly || !dirty) return;

        customTraits.values().removeIf(List::isEmpty);
        customSuppressions.values().removeIf(Set::isEmpty);

        CustomFoodDatapack.saveCustomTraits(customTraits, customSuppressions);
        CustomFoodDatapack.saveCustomProfiles(customProfiles);

        FoodTraitManager.restoreBaseline();
        FoodProfileManager.restoreBaseline();

        for (Map.Entry<String, List<FoodTrait>> entry : customTraits.entrySet()) {
            if (!entry.getKey().startsWith("#")) {
                FoodTraitManager.setItemTraits(ResourceLocation.parse(entry.getKey()), entry.getValue());
            }
        }
        for (Map.Entry<String, Set<String>> entry : customSuppressions.entrySet()) {
            if (!entry.getKey().startsWith("#")) {
                ResourceLocation id = ResourceLocation.parse(entry.getKey());
                Set<String> keys = new LinkedHashSet<>(FoodTraitManager.getBaselineSuppressions(id));
                keys.addAll(entry.getValue());
                FoodTraitManager.setSuppressions(id, keys);
            }
        }
        for (FoodProfile p : customProfiles) {
            FoodProfileManager.upsertProfile(p);
        }

        dirty = false;
        layoutWidgets();
        setStatus("gui.modest_meals.saved_and_applied");
    }

    private void setStatus(String translationKey) {
        this.statusMessage = Component.translatable(translationKey).withStyle(ChatFormatting.GREEN);
        this.statusMessageTicks = 60;
    }

    @Override
    public void onClose() {
        if (this.minecraft == null) return;
        if (!dirty) {
            this.minecraft.setScreen(this.parent);
            return;
        }
        this.minecraft.setScreen(new UnsavedChangesModal(this,
                () -> {
                    saveAndApply();
                    this.minecraft.setScreen(this.parent);
                },
                () -> this.minecraft.setScreen(this.parent)));
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
        if (button == 0 && isOverPaneScrollbar(mouseX, mouseY)) {
            this.draggingPaneScrollbar = true;
            updatePaneScroll(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isOverPaneScrollbar(double mouseX, double mouseY) {
        if (maxPaneScroll() <= 0) return false;
        return mouseX >= scrollbarX() && mouseX < scrollbarX() + GuiUtil.SCROLLBAR_WIDTH
                && mouseY >= contentTop && mouseY < contentBottom;
    }

    private void updatePaneScroll(double mouseY) {
        this.paneScroll = GuiUtil.scrollAmountFromMouse(mouseY, contentTop, scrollTrackHeight(), maxPaneScroll());
        layoutWidgets();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingPaneScrollbar) {
            updatePaneScroll(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingPaneScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (filterDropdown != null && filterDropdown.isOpen()
                && filterDropdown.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        int max = maxPaneScroll();
        if (max > 0 && mouseX >= rightX && mouseX < rightX + rightW + GuiUtil.SCROLLBAR_EXTRA_WIDTH
                && mouseY >= contentTop && mouseY < contentBottom) {
            this.paneScroll = Mth.clamp(this.paneScroll - scrollY * TRAIT_ROW_H, 0, max);
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

        Component titleText = readOnly
                ? Component.translatable("gui.modest_meals.food_editor.title_read_only", this.title)
                : this.title;
        GuiUtil.drawTrimmed(graphics, this.font, titleText, 8, 10, this.width - 16 - statusW, GuiUtil.WHITE);
        if (!status.isEmpty()) {
            graphics.drawString(this.font, status, this.width - 8 - this.font.width(status), 10, GuiUtil.STATUS_GREEN);
        }

        if (readOnly) {
            String hint = GuiUtil.trim(this.font, Component.translatable(
                    "gui.modest_meals.food_editor.read_only_hint").getString(), this.width / 2);
            graphics.drawString(this.font, hint, this.width - GUTTER - this.font.width(hint),
                    this.height - FOOTER_H + 12, GuiUtil.SUBTEXT);
        }

        if (selectedItem != null) {
            renderDetailPane(graphics, mouseX, mouseY, partialTick);
        } else {
            GuiUtil.drawWrappedCentered(graphics, this.font, Component.translatable("gui.modest_meals.no_item_selected"),
                    rightX + rightW / 2, contentTop + (contentBottom - contentTop) / 2, rightW,
                    (contentBottom - contentTop) / GuiUtil.LINE_H, GuiUtil.EMPTY_STATE);
        }

        if (filterDropdown != null) filterDropdown.renderOverlay(graphics, mouseX, mouseY);
    }

    private void renderDetailPane(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ItemStack stack = new ItemStack(selectedItem);
        String id = itemId(selectedItem);
        int contentX = paneContentX();
        int contentW = paneContentWidth();

        graphics.enableScissor(rightX, contentTop, rightX + rightW, contentBottom);

        int slotY = paneY(0);
        GuiUtil.drawSlot(graphics, contentX, slotY);
        graphics.renderFakeItem(stack, contentX + 1, slotY + 1);
        graphics.renderItemDecorations(this.font, stack, contentX + 1, slotY + 1);

        int textX = contentX + 24;
        int textW = contentW - 24;
        GuiUtil.drawTrimmed(graphics, this.font, stack.getHoverName(), textX, slotY + 1, textW, GuiUtil.WHITE);
        GuiUtil.drawTrimmed(graphics, this.font, Component.literal(id), textX, slotY + 11, textW, GuiUtil.DIM);

        int nutrition = FoodValues.nutritionOf(stack);
        boolean isFood = FoodValues.isFood(stack);
        Component nutrText = isFood
                ? Component.translatable("gui.modest_meals.nutrition_value", nutrition).withStyle(ChatFormatting.GOLD)
                : Component.translatable("gui.modest_meals.not_food").withStyle(ChatFormatting.GRAY);
        GuiUtil.drawTrimmed(graphics, this.font, nutrText, contentX, paneY(VY_NUTRITION), contentW, GuiUtil.WHITE);

        int profileY = paneY(VY_PROFILE_HEADER);
        GuiUtil.drawSeparator(graphics, contentX, profileY - 8, contentW);
        int editW = Math.min(90, Math.max(50, contentW / 3));
        GuiUtil.drawTrimmed(graphics, this.font,
                Component.translatable("gui.modest_meals.food_profile_header").withStyle(ChatFormatting.YELLOW),
                contentX, profileY, contentW - editW - 6, 0xFFFF55);

        GuiUtil.drawWrapped(graphics, this.font, profileSummary(), contentX, paneY(VY_PROFILE_SUMMARY), contentW,
                PROFILE_SUMMARY_MAX_LINES, GuiUtil.LABEL);

        int traitsY = paneY(vyTraitsHeader());
        GuiUtil.drawSeparator(graphics, contentX, traitsY - 8, contentW);
        GuiUtil.drawTrimmed(graphics, this.font,
                Component.translatable("gui.modest_meals.traits_header").withStyle(ChatFormatting.YELLOW),
                contentX, traitsY, contentW - editW - 6, 0xFFFF55);

        renderTraitRows(graphics, contentX, contentW);
        renderPaneWidgets(graphics, mouseX, mouseY, partialTick);

        graphics.disableScissor();

        if (maxPaneScroll() > 0) {
            GuiUtil.drawVanillaScrollbar(graphics, scrollbarX(), contentTop, scrollTrackHeight(),
                    paneScroll, maxPaneScroll());
        }
    }

    private void renderTraitRows(GuiGraphics graphics, int contentX, int contentW) {
        List<FoodTrait> traits = getEffectiveTraits(selectedItem);
        int rowsTop = vyTraitsTop();

        if (traits.isEmpty()) {
            GuiUtil.drawWrapped(graphics, this.font,
                    Component.translatable("gui.modest_meals.no_traits")
                            .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY),
                    contentX, paneY(rowsTop) + 4, contentW, EMPTY_TRAITS_H / GuiUtil.LINE_H, GuiUtil.DIM);
            return;
        }

        int textWidth = contentW - 34 - 18 - 10;
        for (int i = 0; i < traits.size(); i++) {
            int rowY = paneY(rowsTop + i * TRAIT_ROW_H);
            if (rowY + TRAIT_ROW_H <= contentTop || rowY >= contentBottom) continue;

            Component tooltip = traits.get(i).getTooltipComponent(1.0, 1.0);
            if (tooltip == null) continue;
            GuiUtil.drawTrimmed(graphics, this.font, tooltip, contentX, rowY + 5, textWidth, GuiUtil.WHITE);
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

    private class PaneButton extends ModButton {
        PaneButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress);
        }

        private boolean withinPane(double mouseY) {
            return mouseY >= contentTop && mouseY < contentBottom;
        }

        @Override
        protected boolean clicked(double mouseX, double mouseY) {
            return withinPane(mouseY) && super.clicked(mouseX, mouseY);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return withinPane(mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }
}
