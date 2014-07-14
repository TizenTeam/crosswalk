// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.core.internal.extension.api.device_capabilities;

import android.util.Log;

import org.xwalk.core.internal.extension.XWalkExtension;
import org.xwalk.core.internal.extension.XWalkExtensionContext;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceCapabilities extends XWalkExtension {
    public static final String JS_API_PATH = "jsapi/device_capabilities_api.js";

    private static final String TAG = "DeviceCapabilities";
    private static final String NAME = "xwalk.experimental.system";

    private DeviceCapabilitiesCPU mCPU;
    private DeviceCapabilitiesCodecs mCodecs;
    private DeviceCapabilitiesDisplay mDisplay;
    private DeviceCapabilitiesMemory mMemory;
    private DeviceCapabilitiesStorage mStorage;

    public DeviceCapabilities(String jsApiContent, XWalkExtensionContext context) {
        super(NAME, jsApiContent, context);

        mCPU = new DeviceCapabilitiesCPU(this, context);
        mCodecs = new DeviceCapabilitiesCodecs(this, context);
        mDisplay = new DeviceCapabilitiesDisplay(this, context);
        mMemory = new DeviceCapabilitiesMemory(this, context);
        mStorage = new DeviceCapabilitiesStorage(this, context);
    }

    private void handleMessage(int instanceID, String message) {
        try {
            JSONObject jsonInput = new JSONObject(message);
            String cmd = jsonInput.getString("cmd");

            if (cmd.equals("addEventListener")) {
                String eventName = jsonInput.getString("eventName");
                handleAddEventListener(eventName);
            } else {
                String asyncCallId = jsonInput.getString("asyncCallId");
                handleGetDeviceInfo(instanceID, asyncCallId, cmd);
            }
        } catch (JSONException e) {
            printErrorMessage(e);
        }
    }

    private void handleGetDeviceInfo(int instanceID, String asyncCallId, String cmd) {
        try {
            JSONObject jsonOutput = new JSONObject();
            if (cmd.equals("getCPUInfo")) {
                jsonOutput.put("data", mCPU.getInfo());
            } else if (cmd.equals("getCodecsInfo")) {
                jsonOutput.put("data", mCodecs.getInfo());
            } else if (cmd.equals("getDisplayInfo")) {
                jsonOutput.put("data", mDisplay.getInfo());
            } else if (cmd.equals("getMemoryInfo")) {
                jsonOutput.put("data", mMemory.getInfo());
            } else if (cmd.equals("getStorageInfo")) {
                jsonOutput.put("data", mStorage.getInfo());
            }
            jsonOutput.put("asyncCallId", asyncCallId);
            this.postMessage(instanceID, jsonOutput.toString());
        } catch (JSONException e) {
            printErrorMessage(e);
        }
    }

    private void handleAddEventListener(String eventName) {
        if (eventName.equals("storageattach") || eventName.equals("storagedetach")) {
            mStorage.registerListener();
        }
    }

    protected void printErrorMessage(JSONException e) {
        Log.e(TAG, e.toString());
    }

    protected JSONObject setErrorMessage(String error) {
        JSONObject out = new JSONObject();
        JSONObject errorMessage = new JSONObject();
        try {
            errorMessage.put("message", error);
            out.put("error", errorMessage);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        return out;
    }

    @Override
    public void onMessage(int instanceID, String message) {
        if (!message.isEmpty()) {
            handleMessage(instanceID, message);
        }
    }

    @Override
    public void onResume() {
        mDisplay.onResume();
        mStorage.onResume();
    }

    @Override
    public void onPause() {
        mDisplay.onPause();
        mStorage.onPause();
    }

    @Override
    public void onDestroy() {
        mDisplay.onDestroy();
        mStorage.onDestroy();
    }
}
