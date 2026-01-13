package io.github.virresh.matvt.helper;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbCrypto;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class AdbKeyManager {
    private static final String LOG_TAG = "MATVTLOG_TAG";
    private static final String IO_PUBLICKEY_FILENAME = "public_key.bin";
    private static final String IO_PRIVATEKEY_FILENAME = "private_key.bin";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static AdbCrypto getAdbCryptoKey(Context ctx) throws NoSuchAlgorithmException {

        // Lambda function to encode data into Base64 for serialization
        AdbBase64 adbBase64Encoder = data -> Base64.getEncoder().encodeToString(data);

        AdbCrypto crypto = null;
        File packageFilesDir;
        File publicKeyFile;
        File privateKeyFile;

        packageFilesDir = ctx.getFilesDir();
        publicKeyFile = new File(packageFilesDir, IO_PUBLICKEY_FILENAME);
        privateKeyFile = new File(packageFilesDir, IO_PRIVATEKEY_FILENAME);

        try {
            if (publicKeyFile.exists() && privateKeyFile.exists()) {
                crypto = AdbCrypto.loadAdbKeyPair(adbBase64Encoder, privateKeyFile, publicKeyFile);
            } else {
                crypto = AdbCrypto.generateAdbKeyPair(adbBase64Encoder);
                crypto.saveAdbKeyPair(privateKeyFile, publicKeyFile);
            }
        } catch (IOException Exception) {
            Log.e(LOG_TAG, "Could not read/write key files");
        } catch (InvalidKeySpecException Exception) {
            Log.e(LOG_TAG, "Could not load keys.");
        } finally {
            if (crypto == null) {
                if (privateKeyFile.exists()) {
                    Log.i(LOG_TAG, "Deleting existing public key file.");
                    privateKeyFile.delete();
                }
                if (publicKeyFile.exists()) {
                    Log.i(LOG_TAG, "Deleting existing private key file.");
                    publicKeyFile.delete();
                }
            }
        }

        return crypto;
    }
}