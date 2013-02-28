package com.wininup.voiladlna;

import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.ArrayAdapter;

/**
 * This class echoes a string called from JavaScript.
 */
public class Dlna extends CordovaPlugin {
	
	private Controller _Ctrl;
	private AndroidUpnpService upnpService;
	private List<Device> deviceList;
	private BrowseRegistryListener registryListener = new BrowseRegistryListener();
	private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;
            // Refresh the list with all known devices
            deviceList.clear();
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }
            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);
            // Search asynchronously for all devices
            upnpService.getControlPoint().search();
        }
        
        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };
    
    private CallbackContext callbackContext = null;
	
	public Dlna()
	{
		_Ctrl = Controller.getInstance(null);
		deviceList = new ArrayList();
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
     */
    private void start()
    {
    	_Ctrl.show("Starting ...");
    	
    	_Ctrl.getContext().getApplicationContext().bindService(
                new Intent(_Ctrl.getContext(), BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }
    
    /**
     * Unbind UPNP Cling discover service
     * */
    private void stop() {
		
    	_Ctrl.show("Stopping ...");
    	
    	if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
    	_Ctrl.getContext().getApplicationContext().unbindService(serviceConnection);
		
	}

    
	private void refresh() {
		
		if (upnpService == null) return;
        _Ctrl.show("Refreshing ...");
        upnpService.getRegistry().removeAllRemoteDevices();
        upnpService.getControlPoint().search();
		
	}
	
	
	private void sendDeviceList()
	{
		JSONObject devices = new JSONObject();
		JSONArray array = new JSONArray();
		
		
        try {
        
        	for (Device device : deviceList) {
        		
        		JSONObject obj = new JSONObject();
        		
        		obj.put("Name", device.getDisplayString());
        		obj.put("Version", device.getVersion());
        		obj.put("Type", device.getType().toString());
			
        		array.put(obj);
			}
        	
        	// bidouille
        	devices.put("devices", array);
        	
        }
		catch (JSONException e) {

			_Ctrl.show("sendDeviceList : " + e.getMessage());
			// Log.e(LOG_TAG, e.getMessage(), e);
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

    /**
     * 
     * receives upnp discovery service event in service thread context
     * 
     * */
    protected class BrowseRegistryListener extends DefaultRegistryListener {

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            
        	// should log
            // "Discovery failed of '" + device.getDisplayString() + "': " + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"));
            
        	deviceRemoved(device);
        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        	deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
        	
        	_Ctrl.runOnUiThread(new Runnable() {
        		public void run() {
		            _Ctrl.show("deviceAdded : " + device.toString());
		            int position = deviceList.indexOf(device);
		            if (position >= 0) {
		                // Device already in the list, re-set new value at same position
		                deviceList.remove(device);
		                deviceList.add(position, device);
		            } else {
		                deviceList.add(device);
		            }
		            sendDeviceList();
        		}
        	});
        }

        public void deviceRemoved(final Device device) {
        	
        	_Ctrl.runOnUiThread(new Runnable() {
        		public void run() {
		        	_Ctrl.show("deviceRemoved : " + device.toString());
		        	deviceList.remove(device);
		        	sendDeviceList();
        		}
        	});
        }
    }
}
