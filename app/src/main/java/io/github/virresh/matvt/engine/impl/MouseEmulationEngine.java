package io.github.virresh.matvt.engine.impl;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import io.github.virresh.matvt.helper.Helper;

public interface MouseEmulationEngine {
    void init(@NonNull AccessibilityService s);
    void updateFromPreferences();
    boolean perform (KeyEvent keyEvent);
}
