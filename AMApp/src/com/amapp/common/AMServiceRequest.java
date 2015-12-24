package com.amapp.common;

import android.content.ContentValues;
import android.util.Log;

import com.amapp.AMApplication;
import com.amapp.Environment;
import com.amapp.common.events.EventBus;
import com.amapp.common.events.ThakorjiTodayUpdateFailedEvent;
import com.amapp.common.events.ThakorjiTodayUpdateSuccessEvent;
import com.smart.caching.SmartCaching;
import com.smart.framework.SmartApplication;
import com.smart.weservice.SmartWebManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dadesai on 12/23/15.
 */
public class AMServiceRequest {

    private static final String TAG = "AMServiceRequest";
    private SmartCaching smartCaching;
    private static AMServiceRequest amServiceRequestInstance;
    private Environment mEnvironment;

    private AMServiceRequest() {
        mEnvironment = AMApplication.getInstance().getEnv();
        smartCaching = new SmartCaching(AMApplication.getInstance().getApplicationContext());
    }

    public static AMServiceRequest getInstance() {
        if(amServiceRequestInstance == null) {
            amServiceRequestInstance = new AMServiceRequest();
        }
        return amServiceRequestInstance;
    }

    public void startThakorjiTodayUpdatesFromServer() {
        HashMap<SmartWebManager.REQUEST_METHOD_PARAMS, Object> requestParams = new HashMap<>();
        requestParams.put(SmartWebManager.REQUEST_METHOD_PARAMS.CONTEXT,AMApplication.getInstance().getApplicationContext());
        requestParams.put(SmartWebManager.REQUEST_METHOD_PARAMS.PARAMS, null);
        requestParams.put(SmartWebManager.REQUEST_METHOD_PARAMS.REQUEST_TYPES, SmartWebManager.REQUEST_TYPE.JSON_OBJECT);
        requestParams.put(SmartWebManager.REQUEST_METHOD_PARAMS.TAG, AMConstants.AMS_Request_Get_Temples_Tag);
        requestParams.put(SmartWebManager.REQUEST_METHOD_PARAMS.URL, getThakorjiTodayUrlWithLatestCachedTimestamp());
        requestParams.put(SmartWebManager.REQUEST_METHOD_PARAMS.REQUEST_METHOD, SmartWebManager.REQUEST_TYPE.GET);
        requestParams.put(SmartWebManager.REQUEST_METHOD_PARAMS.RESPONSE_LISTENER, new SmartWebManager.OnResponseReceivedListener() {

            @Override
            public void onResponseReceived(final JSONObject response, String errorMessage) {

                if (errorMessage != null) {
                    Log.e(TAG, "Error obtaining temple data: " + errorMessage);
                    EventBus.getInstance().post(new ThakorjiTodayUpdateFailedEvent());
                } else {
                    try {
                        smartCaching.cacheResponse(response.getJSONArray("temples"), "temples", true, new SmartCaching.OnResponseParsedListener() {
                            @Override
                            public void onParsed(HashMap<String, ArrayList<ContentValues>> mapTableNameAndData) {
                                if(mapTableNameAndData.get("temples") != null) {
                                    Log.d(TAG, "obtained temple data successfully");
                                    EventBus.getInstance().post(new ThakorjiTodayUpdateSuccessEvent());
                                }
                            }
                        }, /*runOnMainThread*/ false, "images");
                        SmartApplication.REF_SMART_APPLICATION
                                .writeSharedPreferences(AMConstants.KEY_ThakorjiTodayLastUpdatedTimestamp, response
                                        .getString(AMConstants.AMS_RequestParam_ThakorjiToday_LastUpdatedTimestamp));
                    } catch (JSONException e) {
                        EventBus.getInstance().post(new ThakorjiTodayUpdateFailedEvent());
                        e.printStackTrace();
                    }
                }
            }
        });

        SmartWebManager.getInstance(AMApplication.getInstance().getApplicationContext()).addToRequestQueue(requestParams, null, false);
    }


    // gets the latest timestamp cached on the client side
    // and addes it into the ThakorjiToday endpoint as param
    private String getThakorjiTodayUrlWithLatestCachedTimestamp() {
        String endpoint = AMApplication.getInstance().getEnv().getThakorjiTodayEndpoint();
        String lastUpdatedTimeStamp = AMApplication.REF_SMART_APPLICATION
                .readSharedPreferences().getString(AMConstants.KEY_ThakorjiTodayLastUpdatedTimestamp, "");
        if(NetworkConnectionInfo.isMobileDataConnected(AMApplication.getInstance().getApplicationContext())) {
            return String.format(endpoint,lastUpdatedTimeStamp,"slow");
        } else {
            return String.format(endpoint,lastUpdatedTimeStamp,"fast");
        }
    }
}