/**
 * use to make the link between Javascript and Java
 *
 */

package com.wininup.voiladlna;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


/**
 * This class echoes a string called from JavaScript.
 */
public class Dlna extends CordovaPlugin {


	private static final String LOG_TAG = "UPNP Dlna";
	
	private Controller _Ctrl;
	
    private CallbackContext callbackContext = null;
	
	public Dlna()
	{
		_Ctrl = Controller.getInstance(null);
		_Ctrl.setJavascriptWrapper(this);
		_Ctrl.setClingWrapper(new ClingWrapper());
		
	}
	
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
    	
    	_Ctrl.show("execute : " + action);
    	
    	
    	if (action.equals("echo")) {
            String message = args.getString(0); 
            this.echo(message, callbackContext);
            return true;
        }
        else if (action.equals("start")) {
        	
        	if (this.callbackContext != null) {
                callbackContext.error( "Already started");
                return true;
            }
            
            // no result yet we keep the callback 
        	this.callbackContext = callbackContext;
        	PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            
        	this.start();
        	return true;
        }
        else if (action.equals("stop")) {
        	this.stop();
        	this.sendUpdate(new JSONObject(), false); // release status callback in JS side
            this.callbackContext = null;
            callbackContext.success();
            return true;
        }
        else if (action.equals("refresh")) {
        	this.refresh();
        }
        
        
        return false;
    }

    
    /**
     * Stop battery receiver.
     */
    public void onDestroy() {
    	this.stop();
    }

    /**
     * Stop battery receiver.
     */
    public void onReset() {
    	this.stop();
    }
    
	private void echo(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) { 
            callbackContext.success("String  from Native Java code: " + message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    
    
    /**
     * Binding to UPNP Cling discover service
     * 
     * automatically called ?
     */
    private void start()
    {
    	_Ctrl.show("Starting ...");
    	
    	_Ctrl.getClingwrapper().start();
    	
    }
    
    /**
     * Unbind UPNP Cling discover service
     * 
     * automatically called ?
     * */
    private void stop() {
		
    	_Ctrl.show("Stopping ...");
    	
    	_Ctrl.getClingwrapper().stop();
    	
		
	}
	
	private void refresh() {
		_Ctrl.show("Refreshing ...");
	}

    /**
     * use to send upnp devices to javascript by simply sending the list
     * */
	public void sendDeviceList(Object deviceList)
	{
		JSONObject devices = new JSONObject();
		
        try {	
        	devices.put("devices", deviceList);
        }
		catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
	    }
        
        sendUpdate(devices, true);
	}
		
	
	/**
     * Create a new plugin result and send it back to JavaScript
     *
     * @param connection the network info to set as navigator.connection
     */
    private void sendUpdate(JSONObject info, boolean keepCallback) {
        if (this.callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, info);
            result.setKeepCallback(keepCallback);
            this.callbackContext.sendPluginResult(result);
        }
    }

}
