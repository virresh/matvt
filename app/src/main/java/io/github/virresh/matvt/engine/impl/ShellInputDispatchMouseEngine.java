package io.github.virresh.matvt.engine.impl;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

import io.github.virresh.matvt.helper.AdbKeyManager;
import io.github.virresh.matvt.helper.AppPreferences;
import io.github.virresh.matvt.view.MouseCursorView;
import io.github.virresh.matvt.view.OverlayView;

public class ShellInputDispatchMouseEngine implements MouseEmulationEngine {

    private MouseEmulationEngine mEngine;
    private Handler adbHandler;
    private Handler mainHandler;
    private HandlerThread adbThread;

    private AdbCrypto adbCrypto;

    private final int momentumStack;

    private static final String TAG_NAME = "MATVT_ADB_ENGINE";

    // service which started this engine
    private AccessibilityService mService;

    private final PointerControl mPointerControl;
    protected final AppPreferences appPreferences;


    public ShellInputDispatchMouseEngine (Context c, OverlayView ov, AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
        momentumStack = 0;
        // overlay view for drawing mouse
        MouseCursorView mCursorView = new MouseCursorView(c, appPreferences);
        ov.addFullScreenLayer(mCursorView);
        mPointerControl = new PointerControl(ov, mCursorView);
        mPointerControl.disappear();
        Log.i(TAG_NAME, "X, Y: " + mPointerControl.getPointerLocation().x + ", " + mPointerControl.getPointerLocation().y);
    }

    private void sendShellInput(String command) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Runnable runInput = new Runnable() {
                @Override
                public void run() {
                    AdbConnection adbConnection = null;
                    AdbStream stream = null;
                    try {
                        Socket socket = new Socket("localhost", 5555);
                        adbConnection = AdbConnection.create(socket, adbCrypto);
                        adbConnection.connect();
                        stream = adbConnection.open("shell:input " + command);
                    } catch (IOException e) {
                        Log.e(TAG_NAME, "Could not create socket for ADB, error: ", e);
                    } catch (InterruptedException e) {
                        Log.e(TAG_NAME, "ADB Connection Interrupted!", e);
                    } catch (Exception e) {
                        Log.e(TAG_NAME, "Unknown error ---> ", e);
                    } finally {
                        try {
                            if (stream != null) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new ByteArrayInputStream(stream.read()))));
                                stream.close();
                                String output = reader.lines().collect(Collectors.joining("\n"));
                                Log.i(TAG_NAME, "ADB Output --> " + output);
                                notifyUser(output);
                            }
                            if (adbConnection != null) {
                                adbConnection.close();
                            }
                            Log.i(TAG_NAME, "Send command " + command + " successfully");
                        } catch (IOException | InterruptedException e) {
                            Log.e(TAG_NAME, "Connection Close failed! ---> ", e);
                        }
                    }
                }
            };
            adbHandler.post(runInput);
        }
        else {
            Log.e(TAG_NAME, "Need Greater than Android O (api version 26) to use ADB for input");
        }
    }

    public void notifyUser(String text) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mService.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void shellSwipe(Integer x1, Integer y1, Integer x2, Integer y2, Integer duration) {
        sendShellInput("swipe " + x1.toString() + " " + y1.toString() + " " + x2.toString() + " " + y2.toString());
    }

    public void shellTap(Integer x, Integer y) {
        sendShellInput("tap " + x.toString() + " " + y.toString());
    }

    @Override
    public void init(@NonNull AccessibilityService s) {
        this.mService = s;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                adbCrypto = AdbKeyManager.getAdbCryptoKey(s.getApplicationContext());
                adbThread = new HandlerThread("LocalAdbThread");
                adbThread.start();
                adbHandler = new Handler(adbThread.getLooper());
                mainHandler = new Handler();
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG_NAME, "ADB algorithm not available. Regenerate ADB keys.", e);
            }
        }
        else {
            Log.e(TAG_NAME, "Need Greater than Android O (api version 26) to use ADB for input");
        }
    }

    @Override
    public void updateFromPreferences() {

    }

    @Override
    public boolean perform(KeyEvent keyEvent) {
        return false;
    }

    public void destroy() {
        if (adbThread != null) {
            adbThread.quitSafely();
        }
    }
}
