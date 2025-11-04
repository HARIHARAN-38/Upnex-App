package com.upnext.app.ui.animations;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * Utility class for creating smooth UI animations and transitions.
 * Provides common animation effects for better user experience.
 */
public final class AnimationUtils {
    
    private AnimationUtils() {
        // Utility class
    }
    
    /**
     * Creates a smooth color transition animation.
     * 
     * @param component The component to animate
     * @param fromColor Starting color
     * @param toColor Target color
     * @param duration Animation duration in milliseconds
     * @param onComplete Optional callback when animation completes
     */
    public static void animateColorTransition(Component component, Color fromColor, Color toColor, 
                                            int duration, Runnable onComplete) {
        if (fromColor.equals(toColor)) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        
        final int steps = Math.max(20, duration / 16); // ~60 FPS
        final int delay = duration / steps;
        
        final float deltaRed = (toColor.getRed() - fromColor.getRed()) / (float) steps;
        final float deltaGreen = (toColor.getGreen() - fromColor.getGreen()) / (float) steps;
        final float deltaBlue = (toColor.getBlue() - fromColor.getBlue()) / (float) steps;
        final float deltaAlpha = (toColor.getAlpha() - fromColor.getAlpha()) / (float) steps;
        
        Timer timer = new Timer(delay, null);
        timer.addActionListener(new ActionListener() {
            private int step = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                step++;
                
                if (step >= steps) {
                    component.setBackground(toColor);
                    component.repaint();
                    timer.stop();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } else {
                    int red = Math.max(0, Math.min(255, fromColor.getRed() + (int) (deltaRed * step)));
                    int green = Math.max(0, Math.min(255, fromColor.getGreen() + (int) (deltaGreen * step)));
                    int blue = Math.max(0, Math.min(255, fromColor.getBlue() + (int) (deltaBlue * step)));
                    int alpha = Math.max(0, Math.min(255, fromColor.getAlpha() + (int) (deltaAlpha * step)));
                    
                    Color currentColor = new Color(red, green, blue, alpha);
                    component.setBackground(currentColor);
                    component.repaint();
                }
            }
        });
        
        timer.start();
    }
    
    /**
     * Creates a smooth color transition for foreground text.
     */
    public static void animateForegroundTransition(Component component, Color fromColor, Color toColor, int duration) {
        if (fromColor.equals(toColor)) {
            return;
        }
        
        final int steps = Math.max(20, duration / 16);
        final int delay = duration / steps;
        
        final float deltaRed = (toColor.getRed() - fromColor.getRed()) / (float) steps;
        final float deltaGreen = (toColor.getGreen() - fromColor.getGreen()) / (float) steps;
        final float deltaBlue = (toColor.getBlue() - fromColor.getBlue()) / (float) steps;
        
        Timer timer = new Timer(delay, null);
        timer.addActionListener(new ActionListener() {
            private int step = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                step++;
                
                if (step >= steps) {
                    component.setForeground(toColor);
                    component.repaint();
                    timer.stop();
                } else {
                    int red = Math.max(0, Math.min(255, fromColor.getRed() + (int) (deltaRed * step)));
                    int green = Math.max(0, Math.min(255, fromColor.getGreen() + (int) (deltaGreen * step)));
                    int blue = Math.max(0, Math.min(255, fromColor.getBlue() + (int) (deltaBlue * step)));
                    
                    Color currentColor = new Color(red, green, blue);
                    component.setForeground(currentColor);
                    component.repaint();
                }
            }
        });
        
        timer.start();
    }
    
    /**
     * Creates a fade-in animation effect.
     * 
     * @param component The component to fade in
     * @param duration Animation duration in milliseconds
     */
    public static void fadeIn(Component component, int duration) {
        component.setVisible(true);
        
        // For components that support transparency
        Color originalBg = component.getBackground();
        if (originalBg != null) {
            Color transparentBg = new Color(originalBg.getRed(), originalBg.getGreen(), 
                                          originalBg.getBlue(), 0);
            animateColorTransition(component, transparentBg, originalBg, duration, null);
        }
    }
    
    /**
     * Creates a fade-out animation effect.
     * 
     * @param component The component to fade out
     * @param duration Animation duration in milliseconds
     * @param onComplete Callback when fade out is complete
     */
    public static void fadeOut(Component component, int duration, Runnable onComplete) {
        Color originalBg = component.getBackground();
        if (originalBg != null) {
            Color transparentBg = new Color(originalBg.getRed(), originalBg.getGreen(), 
                                          originalBg.getBlue(), 0);
            animateColorTransition(component, originalBg, transparentBg, duration, () -> {
                component.setVisible(false);
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        } else {
            component.setVisible(false);
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
    
    /**
     * Creates a smooth button press animation.
     * 
     * @param button The button to animate
     * @param pressedColor The color when pressed
     * @param originalColor The original button color
     */
    public static void animateButtonPress(Component button, Color pressedColor, Color originalColor) {
        // Quick press animation
        animateColorTransition(button, originalColor, pressedColor, 100, () -> {
            // Return to original color
            animateColorTransition(button, pressedColor, originalColor, 200, null);
        });
    }
    
    /**
     * Creates a pulsing animation effect for notifications or highlights.
     * 
     * @param component The component to pulse
     * @param highlightColor The highlight color
     * @param originalColor The original color
     * @param pulses Number of pulses
     */
    public static void pulseHighlight(Component component, Color highlightColor, Color originalColor, int pulses) {
        animatePulse(component, highlightColor, originalColor, pulses, 0);
    }
    
    private static void animatePulse(Component component, Color highlightColor, Color originalColor, 
                                   int remainingPulses, int currentPulse) {
        if (remainingPulses <= 0) {
            return;
        }
        
        // Pulse to highlight color
        animateColorTransition(component, originalColor, highlightColor, 300, () -> {
            // Pulse back to original color
            animateColorTransition(component, highlightColor, originalColor, 300, () -> {
                // Continue with remaining pulses
                animatePulse(component, highlightColor, originalColor, remainingPulses - 1, currentPulse + 1);
            });
        });
    }
    
    /**
     * Creates a subtle shake animation for error feedback.
     * 
     * @param component The component to shake
     */
    public static void shakeComponent(Component component) {
        final int originalX = component.getX();
        final int shakeDistance = 5;
        final Timer timer = new Timer(50, null);
        
        timer.addActionListener(new ActionListener() {
            private int shakeCount = 0;
            private final int maxShakes = 6;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (shakeCount >= maxShakes) {
                    component.setLocation(originalX, component.getY());
                    timer.stop();
                    return;
                }
                
                int offset = (shakeCount % 2 == 0) ? shakeDistance : -shakeDistance;
                component.setLocation(originalX + offset, component.getY());
                component.repaint();
                shakeCount++;
            }
        });
        
        timer.start();
    }
    
    /**
     * Animation presets for common UI operations.
     */
    public static class Presets {
        public static final int FAST = 150;
        public static final int NORMAL = 300;
        public static final int SLOW = 500;
        
        /**
         * Standard hover animation for buttons.
         */
        public static void buttonHover(Component button, Color hoverColor, Color originalColor) {
            animateColorTransition(button, originalColor, hoverColor, FAST, null);
        }
        
        /**
         * Standard hover exit animation for buttons.
         */
        public static void buttonHoverExit(Component button, Color hoverColor, Color originalColor) {
            animateColorTransition(button, hoverColor, originalColor, FAST, null);
        }
        
        /**
         * Success feedback animation.
         */
        public static void successFeedback(Component component, Color successColor, Color originalColor) {
            pulseHighlight(component, successColor, originalColor, 1);
        }
        
        /**
         * Error feedback animation.
         */
        public static void errorFeedback(Component component) {
            shakeComponent(component);
        }
    }
}