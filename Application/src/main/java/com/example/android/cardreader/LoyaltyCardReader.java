/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.cardreader;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;

import com.example.android.common.logger.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Callback class, invoked when an NFC card is scanned while the device is running in reader mode.
 * <p>
 * Reader mode can be invoked by calling NfcAdapter
 */
public class LoyaltyCardReader implements NfcAdapter.ReaderCallback {
    private static final String TAG = "LoyaltyCardReader";
    // AID for our loyalty card service.
//    private static final String SAMPLE_LOYALTY_CARD_AID = "F222222222";
    // ISO-DEP command HEADER for selecting an AID.
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
//    private static final String SELECT_APDU_HEADER = "00A40400";
    // "OK" status word sent in response to SELECT AID command (0x9000)
//    private static final byte[] SELECT_OK_SW = {(byte) 0x90, (byte) 0x00};

    // Weak reference to prevent retain loop. mAccountCallback is responsible for exiting
    // foreground mode before it becomes invalid (e.g. during onPause() or onStop()).
    private WeakReference<AccountCallback> mAccountCallback;

    public interface AccountCallback {
        public void onAccountReceived(String account);
    }

    public LoyaltyCardReader(AccountCallback accountCallback) {
        mAccountCallback = new WeakReference<AccountCallback>(accountCallback);
    }

    /**
     * Callback when a new tag is discovered by the system.
     * <p>
     * <p>Communication with the card should take place here.
     *
     * @param tag Discovered tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {
        Log.i(TAG, "New tag discovered");
        NfcF nfc = NfcF.get(tag);
        if (nfc != null) {
            byte[] feliCaIDm = tag.getId();
            try {
                nfc.connect();
                byte[] req = readWithoutEncryption(feliCaIDm, 10);
                Log.i(TAG, "req:" + toHex(req));

                byte[] res = nfc.transceive(req);
                Log.i(TAG, "res:" + toHex(res));

                nfc.close();
                final String parseStr = parse(res);
//                Log.i(TAG, "parse:" + parseStr);
//                mAccountCallback.get().onAccountReceived(parseStr);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * @param idm
     * @param size
     * @return
     * @throws IOException
     */
    private byte[] readWithoutEncryption(byte[] idm, int size) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0);
        baos.write(0x06);
        baos.write(idm);
        baos.write(1);
        baos.write(0x0f);
        baos.write(0x09);
        baos.write(size);
        for (int idx = 0; idx < size; idx++) {
            baos.write(0x80);
            baos.write(idx);
        }
        byte[] msg = baos.toByteArray();
        msg[0] = (byte) msg.length;
        return msg;
    }

    /**
     * @param id
     * @return
     */
//    private String toHex(byte[] id) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < id.length; i++) {
//            String hex = "0" + Integer.toString((int) id[i] & 0x0ff, 16);
//            if (hex.length() > 2) {
//                hex = hex.substring(1, 3);
//            }
//            sb.append(" " + i + ":" + hex);
//        }
//        return sb.toString();
//    }

    /**
     * @param res
     * @return
     */
    private String parse(byte[] res) {
        int size = res[12];
        String str = null;
        for (int i = 0; i < size; i++) {
            SuicaHistory rireki = SuicaHistory.parse(res, 13 + i * 16);
                Log.i(TAG, rireki.toString());
//            str += rireki.toString() + "¥n";
        }
        return str;
    }

    /**
     * Build APDU for SELECT AID command. This command indicates which service a reader is
     * interested in communicating with. See ISO 7816-4.
     *
     * @param aid Application ID (AID) to select
     * @return APDU for SELECT AID command
     */
//    public static byte[] BuildSelectApdu(String aid) {
//        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
//        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X", aid.length() / 2) + aid);
//    }

    /**
     * Utility class to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    public static String toHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Utility class to convert a hexadecimal string to a byte string.
     *
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     */
//    public static byte[] HexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i+1), 16));
//        }
//        return data;
//    }

}
