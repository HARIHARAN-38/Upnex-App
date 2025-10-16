package com.upnext.app.ui.theme;

import java.awt.*;
import java.util.Enumeration;
import java.util.Map;
import javax.swing.*;

/**
 * Centralised theme definition to keep Swing widgets visually consistent.
 */
public final class AppTheme {
    public static final Color PRIMARY = new Color(0x1F6FEB);
    public static final Color PRIMARY_DARK = new Color(0x0D3A75);
    public static final Color ACCENT = new Color(0xFF7B72);
    public static final Color BACKGROUND = new Color(0xF5F7FB);
    public static final Color SURFACE = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(0x0F172A);
    public static final Color TEXT_SECONDARY = new Color(0x475569);
    public static final Font PRIMARY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font HEADING_FONT = PRIMARY_FONT.deriveFont(Font.BOLD, 18f);

    private AppTheme() {
    }

    /**
     * Applies the application-wide theme to the current UI session.
     */
    public static void apply() {
        installLookAndFeel();
        applyPalette();
        applyTypography(PRIMARY_FONT);
    }

    /**
     * Exposes the colour palette for custom painting code.
     */
    public static Map<String, Color> palette() {
        return Map.of(
            "primary", PRIMARY,
            "primaryDark", PRIMARY_DARK,
            "accent", ACCENT,
            "background", BACKGROUND,
            "surface", SURFACE,
            "textPrimary", TEXT_PRIMARY,
            "textSecondary", TEXT_SECONDARY
        );
    }

    private static void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
            // If the system L&F is not available we stick to the default.
        }
    }

    private static void applyPalette() {
        UIManager.put("control", BACKGROUND);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Viewport.background", BACKGROUND);
        UIManager.put("ScrollPane.background", BACKGROUND);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.gridColor", new Color(0xE2E8F0));
        UIManager.put("TableHeader.background", PRIMARY);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("Button.background", PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.select", PRIMARY_DARK);
        UIManager.put("ToggleButton.background", SURFACE);
        UIManager.put("ToggleButton.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", PRIMARY_DARK);
        UIManager.put("TextField.selectionBackground", PRIMARY);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.background", SURFACE);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ScrollBar.thumb", new Color(0xCBD5F5));
        UIManager.put("ProgressBar.foreground", PRIMARY);
        UIManager.put("ProgressBar.background", new Color(0xE2E8F0));
    }

    private static void applyTypography(Font baseFont) {
        Enumeration<?> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, baseFont);
            }
        }
        UIManager.put("Label.font", baseFont);
        UIManager.put("Button.font", baseFont);
        UIManager.put("TableHeader.font", HEADING_FONT);
        UIManager.put("TextField.font", baseFont);
        UIManager.put("TextArea.font", baseFont);
        UIManager.put("TabbedPane.font", baseFont);
        UIManager.put("TitledBorder.font", HEADING_FONT);
    }
}
