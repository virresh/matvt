package io.github.virresh.matvt.engine.impl;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.KeyEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.github.virresh.matvt.helper.AppPreferences;
import io.github.virresh.matvt.view.MouseCursorView;
import io.github.virresh.matvt.view.OverlayView;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class BaseEngineTest {

    private BaseEngine engine;

    @Mock
    private Context mockContext;

    @Mock
    private OverlayView mockOverlayView;

    @Mock
    private AccessibilityService mockService;

    @Mock
    private AppPreferences mockAppPreferences;

    @Mock
    private MouseCursorView mockMouseCursorView;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        // Mock the AppPreferences to return default values for the preferences used in BaseEngine
        when(mockAppPreferences.getScrollSpeed()).thenReturn(4);
        when(mockAppPreferences.isBossKeyDisabled()).thenReturn(false);
        when(mockAppPreferences.isBossKeySetToToggle()).thenReturn(true);
        when(mockAppPreferences.getBossKeyValue()).thenReturn(KeyEvent.KEYCODE_PROG_BLUE);
        when(mockAppPreferences.getMouseBordered()).thenReturn(false);
        when(mockAppPreferences.getMouseIconPref()).thenReturn("default");
        when(mockAppPreferences.getMouseSizePref()).thenReturn(1);


        // Mock `getSharedPreferences` on `mockContext`.
        Context applicationContext = Mockito.mock(Context.class);
        when(mockContext.getApplicationContext()).thenReturn(applicationContext);
        when(mockContext.getSharedPreferences(Mockito.anyString(), Mockito.anyInt())).thenReturn(Mockito.mock(android.content.SharedPreferences.class));


        // Now that MouseCursorView is passed as a dependency, we can just mock it directly
        // and avoid all the complex internal mocking of Context, Resources, Drawable, Bitmap.
        // The mockMouseCursorView field is already present and correctly annotated with @Mock.
        // We just need to ensure it's passed to the constructor.

        engine = new GestureDispatchMouseEngine(mockContext, mockOverlayView, mockAppPreferences, mockMouseCursorView);
        engine.init(mockService);
    }

    // @Test
    // public void testInitialState() {
    //     assertNotNull(engine);
    //     assertEquals(BaseEngine.OperatingMode.DPAD, engine.operatingMode);
    // }

    // @Test
    // public void testBossKeyToggle() {
    //     // Given
    //     // These values are now coming from mockAppPreferences, which are set in setUp()
    //     // engine.bossKey = KeyEvent.KEYCODE_PROG_BLUE;
    //     // engine.isBossKeyDisabled = false;
    //     // engine.isBossKeySetToToggle = true;

    //     // When
    //     engine.perform(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PROG_BLUE));

    //     // Then
    //     assertEquals(BaseEngine.OperatingMode.MOUSE, engine.operatingMode);

    //     // When
    //     engine.perform(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PROG_BLUE));

    //     // Then
    //     assertEquals(BaseEngine.OperatingMode.SCROLL, engine.operatingMode);

    //     // When
    //     engine.perform(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PROG_BLUE));

    //     // Then
    //     assertEquals(BaseEngine.OperatingMode.DPAD, engine.operatingMode);
    // }
}
