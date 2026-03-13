package com.sharkev.client.module;

import net.minecraftforge.common.MinecraftForge;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;
    private int keybind;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category, int keybind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keybind = keybind;
        this.enabled = false;
    }

    // =========================================================================
    // Settings system
    // =========================================================================

    public static class Setting<T> {
        private final String name;
        private T value;
        private final T defaultValue;
        private T min, max; // for number settings
        private final SettingType type;

        public enum SettingType { BOOLEAN, NUMBER, MODE }

        // Boolean setting
        @SuppressWarnings("unchecked")
        public Setting(String name, boolean value) {
            this.name = name;
            this.value = (T) Boolean.valueOf(value);
            this.defaultValue = this.value;
            this.type = SettingType.BOOLEAN;
        }

        // Number setting (float with min/max)
        @SuppressWarnings("unchecked")
        public Setting(String name, float value, float min, float max) {
            this.name = name;
            this.value = (T) Float.valueOf(value);
            this.defaultValue = this.value;
            this.min = (T) Float.valueOf(min);
            this.max = (T) Float.valueOf(max);
            this.type = SettingType.NUMBER;
        }

        // Mode setting (string with options stored elsewhere)
        @SuppressWarnings("unchecked")
        public Setting(String name, String value) {
            this.name = name;
            this.value = (T) value;
            this.defaultValue = this.value;
            this.type = SettingType.MODE;
        }

        public String getName() { return name; }
        public T getValue() { return value; }
        public void setValue(T value) { this.value = value; }
        public T getMin() { return min; }
        public T getMax() { return max; }
        public SettingType getType() { return type; }
        public T getDefaultValue() { return defaultValue; }

        public float getFloat() { return (Float) value; }
        public boolean getBool() { return (Boolean) value; }
        public String getMode() { return (String) value; }
    }

    // Mode setting with cycle support
    public static class ModeSetting extends Setting<String> {
        private final String[] modes;

        public ModeSetting(String name, String value, String... modes) {
            super(name, value);
            this.modes = modes;
        }

        public String[] getModes() { return modes; }

        public void cycle() {
            String current = getValue();
            for (int i = 0; i < modes.length; i++) {
                if (modes[i].equals(current)) {
                    setValue(modes[(i + 1) % modes.length]);
                    return;
                }
            }
        }
    }

    // =========================================================================
    // Setting registration helpers - call these in module constructors
    // =========================================================================

    protected Setting<Boolean> addBool(String name, boolean value) {
        Setting<Boolean> s = new Setting<>(name, value);
        settings.add(s);
        return s;
    }

    protected Setting<Float> addSlider(String name, float value, float min, float max) {
        Setting<Float> s = new Setting<>(name, value, min, max);
        settings.add(s);
        return s;
    }

    protected ModeSetting addMode(String name, String value, String... modes) {
        ModeSetting s = new ModeSetting(name, value, modes);
        settings.add(s);
        return s;
    }

    public List<Setting<?>> getSettings() { return settings; }

    // =========================================================================
    // Toggle / enable / disable
    // =========================================================================

    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void onEnable() {
        try { MinecraftForge.EVENT_BUS.register(this); } catch (Exception ignored) {}
    }

    public void onDisable() {
        try { MinecraftForge.EVENT_BUS.unregister(this); } catch (Exception ignored) {}
    }

    // =========================================================================
    // Getters / setters
    // =========================================================================

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public Category getCategory()  { return category; }
    public boolean isEnabled()     { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getKeybind()        { return keybind; }
    public void setKeybind(int keybind) { this.keybind = keybind; }

    /** @deprecated Use {@link #getKeybind()} instead. */
    @Deprecated
    public int getKey()            { return keybind; }
    /** @deprecated Use {@link #setKeybind(int)} instead. */
    @Deprecated
    public void setKey(int key)    { this.keybind = key; }
}
