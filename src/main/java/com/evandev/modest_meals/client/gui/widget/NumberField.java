package com.evandev.modest_meals.client.gui.widget;

import com.evandev.modest_meals.client.gui.util.GuiUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.DoubleConsumer;

public class NumberField extends ModEditBox {
    private final double min;
    private final double max;
    private final int decimals;
    private final DoubleConsumer sink;

    private boolean valid = true;

    public NumberField(Font font, int x, int y, int width, int height, Component message,
                       double min, double max, int decimals, double initial, DoubleConsumer sink) {
        super(font, x, y, width, height, message);
        this.min = min;
        this.max = max;
        this.decimals = decimals;
        this.sink = sink;

        this.setMaxLength(16);
        this.setFilter(s -> s.isEmpty() || s.matches(decimals > 0 ? "-?\\d*\\.?\\d*" : "-?\\d*"));
        this.setValue(format(initial));
        this.setResponder(this::onTextChanged);
        this.valid = true;
    }

    public static NumberField integer(Font font, int x, int y, int w, int h, Component msg,
                                      int min, int max, int initial, DoubleConsumer sink) {
        return new NumberField(font, x, y, w, h, msg, min, max, 0, initial, sink);
    }

    public static NumberField decimal(Font font, int x, int y, int w, int h, Component msg,
                                      double min, double max, double initial, DoubleConsumer sink) {
        return new NumberField(font, x, y, w, h, msg, min, max, 2, initial, sink);
    }

    private String format(double v) {
        if (decimals <= 0) return String.valueOf((long) v);
        String s = String.format(Locale.ROOT, "%." + decimals + "f", v);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "");
            if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private void onTextChanged(String text) {
        Double parsed = parse(text);
        this.valid = parsed != null;
        if (parsed != null && sink != null) {
            sink.accept(parsed);
        }
    }

    private Double parse(String text) {
        String t = text.trim();
        if (t.isEmpty() || t.equals("-") || t.equals(".") || t.equals("-.")) return null;
        try {
            double v = Double.parseDouble(t);
            if (Double.isNaN(v) || Double.isInfinite(v)) return null;
            return (v < min || v > max) ? null : v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean isValid() {
        return valid;
    }

    public double doubleValue() {
        Double v = parse(this.getValue());
        return v != null ? v : min;
    }

    public float floatValue() {
        return (float) doubleValue();
    }

    public int intValue() {
        return (int) Math.round(doubleValue());
    }

    public void setNumber(double value) {
        this.setValue(format(value));
    }

    public Component rangeHint() {
        return Component.literal(format(min) + " - " + format(max));
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        if (!valid && this.isVisible()) {
            guiGraphics.renderOutline(this.getX(), this.getY(), this.getWidth(), this.getHeight(),
                    0xFF000000 | GuiUtil.ERROR_RED);
        }
    }
}
