/**
 * use to make the link between Javascript and Java
 *
 */

package com.wininup.voiladlna;

import java.util.List;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.util.Log;


/**
 * This class echoes a string called from JavaScript.
 */
public class Dlna extends CordovaPlugin {


	private static final String LOG_TAG = "Web UPNP Dlna";
	
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
        	this.sendDevicesUpdate(null, false); // release status callback in JS side
        	this.sendBrowseUpdate(null, null, null, false); // release status callback in JS side        	
            this.devicesChangedCallback = null;
            this.browseDeviceCallback = null;
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
     * Create a networkService object and send it to JavaScript
     *
     * navigator.getNetworkServices callback
     *
     */
	public void sendDevicesUpdate(List<Device> deviceList, boolean keepCallback) {
        if (this.devicesChangedCallback != null) {
        	
        	try {
        		JSONObject jsObject = new JSONObject();
        		
        		if (deviceList != null)
        		{	        	
        			JSONArray jsDevices = new JSONArray();
    	        	
		        	for (Device device : deviceList) {
		        		JSONObject jsDevice = new JSONObject();
		        		jsDevice.put("id", device.getIdentity().getUdn().toString());
		        		jsDevice.put("name", device.getDetails().getFriendlyName());
        				jsDevice.put("type", "upnp:" + device.getType().toString()); // prefixing "upnp:" to comply w3c
						jsDevice.put("url", "config url"); // device.getType().getNamespace(), // device.getDetails().getBaseURL().toString(), // control url to define
						jsDevice.put("config", "config ?");
						jsDevice.put("online", true);
		        		
		        		jsDevices.put(jsDevice);
		        	}
		        	
		        	jsObject.put("networkServices", jsDevices);
        		}
        		
	            PluginResult result = new PluginResult(PluginResult.Status.OK, jsObject);
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
	 * @param deviceId 
	 * @param containerId 
     *
     */
    public void sendBrowseUpdate(DIDLContent didl, String containerId, String deviceId, boolean keepCallback) {
        if (this.browseDeviceCallback != null) {
        	
        	JSONObject jsContainer = new JSONObject();
        	
        	try {
            	
            		
            	if (didl != null) {

            		JSONArray containers = new JSONArray();
            		JSONArray items = new JSONArray();
            		
            		jsContainer.put("id", containerId);
            		jsContainer.put("deviceId", deviceId);
            		
            		// items
            		for (Item item : didl.getItems()) {
            			JSONObject jsItem = new JSONObject();
            			jsItem.put("id", item.getId());
            			jsItem.put("name", item.getTitle());
            			jsItem.put("uri", "TODO uri");
            			jsItem.put("encoding", "TODO encoding");
            			
            			items.put(jsItem);
            		}
            		jsContainer.put("items", items);
            		
            		// containers  @todo use recursive call to populate tree
            		for (Container container : didl.getContainers()) {
            			
            			JSONObject tmpContainer = new JSONObject();
            			tmpContainer.put("id", container.getId());
            			tmpContainer.put("name", container.getTitle());
            			tmpContainer.put("containersCount", container.getContainers().size());
            			tmpContainer.put("itemsCount", container.getItems().size());
            			
            			containers.put(tmpContainer);
            		}
            		jsContainer.put("containers", containers);
            	}
	        	
	            PluginResult result = new PluginResult(PluginResult.Status.OK, jsContainer);
	            result.setKeepCallback(keepCallback);
	            this.browseDeviceCallback.sendPluginResult(result);
	        }
    		catch (Exception e) {
    			Log.e(LOG_TAG, "sendBrowseUpdate " + e.getMessage(), e);
    	    }
        }
    }
    
    class NetworkService
    {
    	
    	public String    id;
    	public String    name;
    	public String    type;
    	public String    url;
    	public String    config;
    	public boolean   online;
    	
    	/// @todo	iconsUri
    	// String[]	iconsUri;
    	
		public NetworkService(String id, String name, String type, String url, String config, boolean online) {
			super();
			this.id = id;
			this.name = name;
			this.type = type;
			this.url = url;
			this.config = config;
			this.online = online;
		}
    }
    
    class ContentDirectoryContainer
    {
    	
    	public String   id;
    	public String 	name;
    	public int		itemsCount;
    	public int		containersCount;
    	
		public ContentDirectoryContainer(String id, String name, int itemsCount, int containersCount) {
			super();
			this.id = id;
			this.name = name;
			this.itemsCount = itemsCount;
			this.containersCount = containersCount;
		}
    	
    	
    	
    }
    
    class ContentDirectoryItem
    {
    	public String   id;
    	public String 	name;
    	public String   url;
    	public String   encoding;
    	
		public ContentDirectoryItem(String id, String name, String url, String encoding) {
			super();
			this.id = id;
			this.name = name;
			this.url = url;
			this.encoding = encoding;
		}
    	
    	
    }
}
