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
	
    private CallbackContext devicesChangedCallback = null;
	private CallbackContext browseDeviceCallback = null;
	
	public Dlna()
	{
		_Ctrl = Controller.getInstance(null);
		_Ctrl.setJavascriptWrapper(this);
		_Ctrl.setClingWrapper(new ClingWrapper());
		
	}
	
	/**
	 * 
	 *	Called by Javascript 
	 * 
	 */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
    	
    	_Ctrl.show("execute : " + action);
    	
    	
    	if (action.equals("echo")) {
            String message = args.getString(0); 
            this.echo(message, callbackContext);
            return true;
        }
        else if (action.equals("registerDeviceDiscovery")) {
        	
        	if (this.devicesChangedCallback != null) {
                callbackContext.error( "registerDeviceDiscovery already registered");
                return true;
            }
            
            // no result yet we keep the callback 
        	this.devicesChangedCallback = callbackContext;
        	PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            
        	this.start();
        	return true;
        }
        else if (action.equals("stop")) {
        	this.stop();
        	this.sendDevicesUpdate(new JSONObject(), false); // release status callback in JS side
            this.devicesChangedCallback = null;
            callbackContext.success();
            return true;
        }
        else if (action.equals("refresh")) {
        	_Ctrl.getClingwrapper().refresh();
        	return true;
        }
        else if (action.equals("registerBrowseDevice")) {
        	if (this.browseDeviceCallback != null) {
                callbackContext.error( "registerBrowseDevice already registered");
                return true;
            }
            
            // no result yet we keep the callback 
        	this.browseDeviceCallback = callbackContext;
        	PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }
        else if (action.equals("browseDevice")) {
        	
        	String deviceUdn = args.getString(0);
        	String containerId = args.getString(1);
        	
        	_Ctrl.getClingwrapper().browse(deviceUdn, containerId);
        	return true;
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
     * Create a new plugin result and send it back to JavaScript
     *
     */
	public void sendDevicesUpdate(Object deviceList, boolean keepCallback) {
        if (this.devicesChangedCallback != null) {
        	
        	try {
	        	JSONObject devices = new JSONObject();
	        	devices.put("devices", deviceList);

	            PluginResult result = new PluginResult(PluginResult.Status.OK, devices);
	            result.setKeepCallback(keepCallback);
	            this.devicesChangedCallback.sendPluginResult(result);
	            
        	}
    		catch (Exception e) {
    			Log.e(LOG_TAG, "sendDevicesUpdate " + e.getMessage(), e);
    	    }
        }
    }
    
    
	/**
     * Create a new plugin result and send it back to JavaScript
     *
     */
    public void sendBrowseUpdate(Object container, boolean keepCallback) {
        if (this.browseDeviceCallback != null) {
        	
        	try {

	        	JSONObject containerJson = new JSONObject();
	        	containerJson.put("container", container);
	        	
	        	Log.e(LOG_TAG, "sendBrowseUpdate " + containerJson.toString());
	        	
	            PluginResult result = new PluginResult(PluginResult.Status.OK, containerJson);
	            result.setKeepCallback(keepCallback);
	            this.browseDeviceCallback.sendPluginResult(result);
	        }
    		catch (Exception e) {
    			Log.e(LOG_TAG, "sendBrowseUpdate " + e.getMessage(), e);
    	    }
        }
    }

}
