package com.evandev.modest_meals.client.gui.editor;

import com.evandev.modest_meals.client.gui.util.GuiUtil;
import com.evandev.modest_meals.client.gui.widget.DropdownWidget;
import com.evandev.modest_meals.client.gui.widget.ModButton;
import com.evandev.modest_meals.client.gui.widget.NumberField;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;

public class FormBuilder {
    public static final int ROW_H = 18;
    private static final int ROW_GAP = 6;
    private static final int LABEL_GAP = 6;

    private final Font font;
    private final int x;
    private final int y;
    private final int width;
    private final Runnable onChange;

    private final List<Placed> placed = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private final List<NumberField> numberFields = new ArrayList<>();
    private final List<DropdownWidget> dropdowns = new ArrayList<>();

    private final Map<String, Object> seed;
    private final Map<String, Object> values = new HashMap<>();

    private int cursorY;

    public FormBuilder(Font font, int x, int y, int width, Runnable onChange) {
        this(font, x, y, width, onChange, Map.of());
    }

    public FormBuilder(Font font, int x, int y, int width, Runnable onChange, Map<String, Object> seed) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.width = width;
        this.cursorY = y;
        this.onChange = onChange;
        this.seed = seed == null ? Map.of() : seed;
    }

    public Map<String, Object> values() {
        return values;
    }

    private int labelWidth() {
        return Math.max(40, (int) (width * 0.46));
    }

    private int controlX() {
        return x + labelWidth() + LABEL_GAP;
    }

    private int controlWidth() {
        return Math.max(30, width - labelWidth() - LABEL_GAP);
    }

    private void addLabel(Component text) {
        labels.add(new Label(text, x, cursorY + (ROW_H - 8) / 2, labelWidth()));
    }

    private void advance() {
        cursorY += ROW_H + ROW_GAP;
    }

    private void changed() {
        if (onChange != null) onChange.run();
    }

    public FormBuilder integer(String labelKey, int min, int max, int initial, DoubleConsumer sink) {
        return number(labelKey, min, max, 0, initial, sink);
    }

    public FormBuilder decimal(String labelKey, double min, double max, double initial, DoubleConsumer sink) {
        return number(labelKey, min, max, 2, initial, sink);
    }

    public FormBuilder number(String labelKey, double min, double max, int decimals, double initial, DoubleConsumer sink) {
        addLabel(Component.translatable(labelKey));

        double start = initial;
        if (seed.get(labelKey) instanceof Number carried) {
            start = Math.max(min, Math.min(max, carried.doubleValue()));
            sink.accept(start);
        }
        values.put(labelKey, start);

        NumberField field = new NumberField(font, controlX(), cursorY, controlWidth(), ROW_H,
                Component.translatable(labelKey), min, max, decimals, start, v -> {
            values.put(labelKey, v);
            sink.accept(v);
            changed();
        });
        numberFields.add(field);
        placed.add(new Placed(field, cursorY));
        advance();
        return this;
    }

    public FormBuilder toggle(String labelKey, boolean initial, Consumer<Boolean> sink) {
        addLabel(Component.translatable(labelKey));
        boolean start = initial;
        if (seed.get(labelKey) instanceof Boolean carried) {
            start = carried;
            sink.accept(start);
        }
        values.put(labelKey, start);

        boolean[] state = {start};
        ModButton button = new ModButton(controlX(), cursorY, controlWidth(), ROW_H,
                state[0] ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF, b -> {
            state[0] = !state[0];
            b.setMessage(state[0] ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
            values.put(labelKey, state[0]);
            sink.accept(state[0]);
            changed();
        });
        placed.add(new Placed(button, cursorY));
        advance();
        return this;
    }

    public FormBuilder choice(String labelKey, List<String> choices, Function<String, Component> labeller,
                              String selected, Consumer<String> sink) {
        addLabel(Component.translatable(labelKey));

        String start = selected;
        if (seed.get(labelKey) instanceof String carried && choices.contains(carried)) {
            start = carried;
            sink.accept(start);
        }
        this.values.put(labelKey, start);

        DropdownWidget dropdown = new DropdownWidget(controlX(), cursorY, controlWidth(), ROW_H,
                Component.empty(), choices, labeller, v -> {
            this.values.put(labelKey, v);
            sink.accept(v);
            changed();
        }).searchable();
        dropdown.setSelectedValue(start);
        dropdowns.add(dropdown);
        placed.add(new Placed(dropdown, cursorY));
        advance();
        return this;
    }

    public FormBuilder note(String labelKey) {
        labels.add(new Label(Component.translatable(labelKey), x, cursorY, width));
        advance();
        return this;
    }

    public List<AbstractWidget> widgets() {
        return placed.stream().map(Placed::widget).toList();
    }

    public List<DropdownWidget> dropdowns() {
        return dropdowns;
    }

    public int getHeight() {
        return placed.isEmpty() && labels.isEmpty() ? 0 : Math.max(0, cursorY - y - ROW_GAP);
    }

    public boolean isValid() {
        return numberFields.stream().allMatch(NumberField::isValid);
    }

    public boolean anyDropdownOpen() {
        return dropdowns.stream().anyMatch(DropdownWidget::isOpen);
    }

    public void closeDropdowns() {
        dropdowns.forEach(DropdownWidget::close);
    }

    public void applyScroll(double scroll, int viewTop, int viewBottom) {
        for (Placed p : placed) {
            int newY = p.baseY() - (int) scroll;
            p.widget().setY(newY);
            p.widget().visible = newY >= viewTop - ROW_H && newY + ROW_H <= viewBottom + ROW_H;
            p.widget().active = p.widget().visible;
        }
    }

    public void renderLabels(GuiGraphics graphics, double scroll) {
        for (Label l : labels) {
            GuiUtil.drawTrimmed(graphics, font, l.text(), l.x(), l.baseY() - (int) scroll, l.maxWidth(), GuiUtil.LABEL);
        }
    }

    private record Placed(AbstractWidget widget, int baseY) {
    }

    private record Label(Component text, int x, int baseY, int maxWidth) {
    }
}
