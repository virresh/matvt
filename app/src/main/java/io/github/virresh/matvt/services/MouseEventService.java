package io.github.virresh.matvt.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import io.github.virresh.matvt.BuildConfig;
import io.github.virresh.matvt.engine.impl.MouseEmulationEngine;
import io.github.virresh.matvt.engine.impl.PointerControl;
import io.github.virresh.matvt.helper.Helper;
import io.github.virresh.matvt.helper.KeyDetection;
import io.github.virresh.matvt.view.OverlayView;
import okhttp3.OkHttpClient;

import static io.github.virresh.matvt.engine.impl.MouseEmulationEngine.bossKey;
import static io.github.virresh.matvt.engine.impl.MouseEmulationEngine.scrollSpeed;

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

public class MouseEventService extends AccessibilityService {

    private MouseEmulationEngine mEngine;
    private OkHttpClient okclient;
    private Handler adbHandler;
    private Handler mainHandler;
    private HandlerThread adbThread;

    private AdbCrypto adbCrypto;

    private static String TAG_NAME = "MATVT_SERVICE";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {}

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        super.onKeyEvent(event);
        new KeyDetection(event);
        Log.i(TAG_NAME, "MATVT Received Key => " + event.getKeyCode() + ", Action => " + event.getAction() + ", Repetition value => " + event.getRepeatCount() + ", Scan code => " + event.getScanCode());
        if (Helper.isAnotherServiceInstalled(this) &&
                event.getKeyCode() == KeyEvent.KEYCODE_HOME) return true;
        if (Helper.isOverlayDisabled(this)) return false;
        return mEngine.perform(event);
    }

    @Override
    public void onInterrupt() {}

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG_NAME, "Starting service initialization sequence. App version " + BuildConfig.VERSION_NAME);
        bossKey = KeyEvent.KEYCODE_VOLUME_MUTE;
        PointerControl.isBordered = Helper.getMouseBordered(this);
        scrollSpeed = Helper.getScrollSpeed(this);
        MouseEmulationEngine.isBossKeyDisabled = Helper.isBossKeyDisabled(this);
        MouseEmulationEngine.isBossKeySetToToggle = Helper.isBossKeySetToToggle(this);
        if (Helper.isOverriding(this)) bossKey = Helper.getBossKeyValue(this);
        if (Settings.canDrawOverlays(this)) init();
    }

    private void init() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                adbCrypto = Helper.getAdbCryptoKey(this);
                adbThread = new HandlerThread("LocalAdbThread");
                adbThread.start();
                adbHandler = new Handler(adbThread.getLooper());
                mainHandler = new Handler();
            }
            else {
                Log.e(TAG_NAME, "Need Greater than Android O (api version 26) to use ADB for input");
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG_NAME, "Could not create crypto keys for ADB, error: ", e);
        } catch (Exception e) {
            Log.e(TAG_NAME, "Unknown error ---> ", e);
        }
        okclient = new OkHttpClient();
        if (Helper.helperContext != null) Helper.helperContext = this;
        OverlayView mOverlayView = new OverlayView(this);
        AccessibilityServiceInfo asi = this.getServiceInfo();
        if (asi != null) {
            asi.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            this.setServiceInfo(asi);
        }
        Log.i(TAG_NAME, "Configuration -- Scroll Speed " + scrollSpeed);
        Log.i(TAG_NAME, "Configuration -- Boss Key Disabled " + MouseEmulationEngine.isBossKeyDisabled);
        Log.i(TAG_NAME, "Configuration -- Boss Key Toggleable " + MouseEmulationEngine.isBossKeySetToToggle);
        Log.i(TAG_NAME, "Configuration -- Is Bordered " + PointerControl.isBordered);
        Log.i(TAG_NAME, "Configuration -- Boss Key value " + bossKey);

        mEngine = new MouseEmulationEngine(this, mOverlayView);
        mEngine.init(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adbThread != null) {
            adbThread.quitSafely();
        }
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
                Toast.makeText(MouseEventService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void shellSwipe(Integer x1, Integer y1, Integer x2, Integer y2, Integer duration) {
//        Request request = new Request.Builder()
//                .url("http://192.168.0.129:5000/swipe/" + x1.toString() + "/" + y1.toString() + "/" + x2.toString() + "/" + y2.toString() + "/" + duration.toString())
//                .build();

//        okclient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Log.i(TAG_NAME, "Call failed with exception ===> " + e.toString());
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                Log.i(TAG_NAME, "Succeeded ? ===> " + response.isSuccessful());
//            }
//        });
        sendShellInput("swipe " + x1.toString() + " " + y1.toString() + " " + x2.toString() + " " + y2.toString());
    }

    public void shellTap(Integer x, Integer y) {
        sendShellInput("tap " + x.toString() + " " + y.toString());
//        Request request = new Request.Builder()
//                .url("http://192.168.0.129:5000/click/" + x.toString() + "/" + y.toString())
//                .build();
//        okclient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Log.i(TAG_NAME, "Call failed with exception ===> " + e.toString());
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                Log.i(TAG_NAME, "Succeeded ? ===> " + response.isSuccessful());
//            }
//        });
//        Handler h = new Handler();
//        h.post(new Runnable() {
//            @Override
//            public void run() {
//
//
//                        String[] commands = {"input", "tap", x.toString(), y.toString()};
//                String commands = "sh -c /data/local/tmp/adb-mini whoami";
//                try {
//                    Process p = Runtime.getRuntime().exec(commands);
//                    p.waitFor();
//                    if (p.exitValue() == 0) {
//                        Log.i(TAG_NAME, "Input Request sent");
//                        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                        String bufferOp = null;
//                        while ((bufferOp = br.readLine()) != null) {
//                            Log.i(TAG_NAME, bufferOp);
//                        }
//                    }
//                    else {
//                        Log.i(TAG_NAME, "Input Request failed " + p.exitValue());
//                        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//                        String bufferOp = null;
//                        while ((bufferOp = br.readLine()) != null) {
//                            Log.i(TAG_NAME, bufferOp);
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Log.i(TAG_NAME, "Input Request threw error with " + e.toString());
//                }
//            }
//        });
    }

}
