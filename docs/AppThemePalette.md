# UpNext App Theme Palette

This document provides a reference to the color and typography values defined in the application's theme.

## Color Palette

| Name | Hex Value | RGB Value | Preview | Usage |
|------|-----------|-----------|---------|-------|
| PRIMARY | #1F6FEB | rgb(31, 111, 235) | ![#1F6FEB](https://via.placeholder.com/15/1F6FEB/1F6FEB.png) | Primary buttons, main navigation elements, selected items |
| PRIMARY_DARK | #0D3A75 | rgb(13, 58, 117) | ![#0D3A75](https://via.placeholder.com/15/0D3A75/0D3A75.png) | Button hover states, secondary actions |
| ACCENT | #FF7B72 | rgb(255, 123, 114) | ![#FF7B72](https://via.placeholder.com/15/FF7B72/FF7B72.png) | Call-to-action elements, important highlights |
| BACKGROUND | #F5F7FB | rgb(245, 247, 251) | ![#F5F7FB](https://via.placeholder.com/15/F5F7FB/F5F7FB.png) | Main application background |
| SURFACE | #FFFFFF | rgb(255, 255, 255) | ![#FFFFFF](https://via.placeholder.com/15/FFFFFF/FFFFFF.png) | Cards, dialogs, form fields |
| TEXT_PRIMARY | #0F172A | rgb(15, 23, 42) | ![#0F172A](https://via.placeholder.com/15/0F172A/0F172A.png) | Main content text |
| TEXT_SECONDARY | #475569 | rgb(71, 85, 105) | ![#475569](https://via.placeholder.com/15/475569/475569.png) | Secondary text, labels, hints |

## Typography

| Name | Font Family | Weight | Size | Usage |
|------|------------|--------|------|-------|
| PRIMARY_FONT | Segoe UI | Regular | 14px | Default text throughout the application |
| HEADING_FONT | Segoe UI | Bold | 18px | Section headers, table headers, titled borders |

## Component Theme Mappings

| Swing Component | Property | Theme Value |
|----------------|----------|-------------|
| Panel | background | BACKGROUND (#F5F7FB) |
| Button | background | PRIMARY (#1F6FEB) |
| Button | foreground | WHITE (#FFFFFF) |
| Button (selected) | background | PRIMARY_DARK (#0D3A75) |
| TextField | background | SURFACE (#FFFFFF) |
| TextField | foreground | TEXT_PRIMARY (#0F172A) |
| Label | foreground | TEXT_PRIMARY (#0F172A) |
| Table | background | SURFACE (#FFFFFF) |
| TableHeader | background | PRIMARY (#1F6FEB) |
| TableHeader | foreground | WHITE (#FFFFFF) |
| ProgressBar | foreground | PRIMARY (#1F6FEB) |

## Usage Guidelines

1. Use PRIMARY for main interactive elements that drive user actions
2. Use ACCENT sparingly for elements that require immediate attention
3. Maintain contrast ratios between text and background colors for accessibility
4. When creating custom components, use the `AppTheme.palette()` map to access theme colors
5. Use the following code to apply the theme at application startup:
   ```java
   AppTheme.apply();
   ```

## Extending the Theme

When creating custom painted components, use the palette accessor:

```java
Map<String, Color> colors = AppTheme.palette();
g2d.setColor(colors.get("primary"));
// Custom painting code
```