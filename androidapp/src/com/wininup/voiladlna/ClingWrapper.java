/**
 * 
 * @author tibo
 * 
 * 
 * To start Av transport look at :
 * http://code.google.com/p/ndphu-training-projects/source/browse/DLNADemo/src/com/gui/BrowseRegistryListener.java?r=4
 * 
 * 
 * */

package com.wininup.voiladlna;

import java.util.ArrayList;
import java.util.List;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;


public class ClingWrapper {

	
	
	/**
	 * @todo  find a better way to specify Type
	 * */
	private final static String deviceTypeMediaServer = "urn:schemas-upnp-org:device:MediaServer:1";

	
	private Controller _Ctrl;
	
	private static final String LOG_TAG = "UPNP ClingWrapper";

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
	
	/**
	 * 
	 */
	public ClingWrapper()
	{
		_Ctrl = Controller.getInstance(null);
		deviceList = new ArrayList();
	}
	
	/**
	 * bind to cling upnp devices discover service
	 */
	public void start()
	{
	
		try {
		
		
		_Ctrl.getContext().getApplicationContext().bindService(
                new Intent(_Ctrl.getContext(), BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
		
		}
		catch (Exception e) {
			Log.d(LOG_TAG, e.getMessage(), e);
		}
		
		int test = 5;
		test++;
		int toto = test;
		
	}
	
	/**
	 * unbind from cling upnp devices discover service
	 */
	public void stop()
	{
		if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
    	_Ctrl.getContext().getApplicationContext().unbindService(serviceConnection);
	}
	
	
	/**
	 * not use yet 
	 * 
	 * should restart the discover pocess
	 * 
	 */
	private void refresh() {
		if (upnpService == null) return;
        
        upnpService.getRegistry().removeAllRemoteDevices();
        upnpService.getControlPoint().search();
		
	}
	

//	public Service createService() {
//		LocalService<AbstractContentDirectoryService> service =
//                 new AnnotationLocalServiceBinder().read(AbstractContentDirectoryService.class);
//        service.setManager(
//                 new DefaultServiceManager<AbstractContentDirectoryService>(service, null) {
//                     @Override
//                     protected AbstractContentDirectoryService createServiceInstance() throws Exception {
//                         return new MP3ContentDirectory();
//                     }
//                 }
//         );
//         return service;
//     }
	
	public void browse(final Device device){
		
			try
			{
				new Thread(new Runnable() {

					@Override
					public void run() {

							Service service = device.findService(new UDAServiceId("ContentDirectory"));
							ActionCallback simpleBrowseAction = new Browse(service, "0", BrowseFlag.DIRECT_CHILDREN) {
								
								@Override
							    public void received(ActionInvocation actionInvocation, DIDLContent didl) {
					
									List<Container> containers = didl.getContainers();
									List<Item> items = didl.getItems();
									
									_Ctrl.show("found " + containers.size() + "containers and " + items.size() + " items" );
									
									if (containers.size() > 0) {
										Container container = containers.get(0);										
										Log.i(LOG_TAG, container.getTitle() + " from " + device.getDetails().getFriendlyName());
									}
									else if (items.size() > 0) {
										Item item = items.get(0);										
										Log.i(LOG_TAG, item.getTitle() + " from " + device.getDetails().getFriendlyName());
									}
									
							    }
					
							    @Override
							    public void updateStatus(Status status) {
							        // Called before and after loading the DIDL content
							    }
					
							    @Override
							    public void failure(ActionInvocation invocation,
							                        UpnpResponse operation,
							                        String defaultMsg) {
							        // Something wasn't right...
							    }
							};
							
							simpleBrowseAction.setControlPoint(upnpService.getControlPoint()); 
							simpleBrowseAction.run();
				
					}

				}).start();
				
			}
			catch (Exception ee)
			{
				Log.d(LOG_TAG, ee.getMessage(), ee);
			}
			
			
	}
	
	private void browse2(Action getFiles, AndroidUpnpService upnpService) {
		
        ActionInvocation mActionInvocation=new ActionInvocation(getFiles);
                    mActionInvocation.setInput("ObjectID", "0");
                    mActionInvocation.setInput("BrowseFlag", "BrowseDirectChildren");
                    mActionInvocation.setInput("Filter", "*");
                    mActionInvocation.setInput("StartingIndex", new UnsignedIntegerFourBytes(0));
                    mActionInvocation.setInput("RequestedCount", new UnsignedIntegerFourBytes(2));
                    mActionInvocation.setInput("SortCriteria", "+dc:title");
                    ActionCallback mActionCallback=new ActionCallback(mActionInvocation) {
                           
                            @Override
                            public void success(ActionInvocation actionInvocation) {
                            	String result=actionInvocation.getOutput("Result").getValue().toString();
                            	_Ctrl.show("browse2 success");
                            	
                            	Log.i("UPNP", result);
                            	
                            }
                           
                            @Override
                            public void failure(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
                            	_Ctrl.show("browse2 failure");
                            }
                    };
                    upnpService.getControlPoint().execute(mActionCallback);
            } 

	
  // class SetAVTransportURI extends ActionInvocation {

    
    /**
     * 
     * receives upnp discovery service event in service thread context
     * 
     * */
    protected class BrowseRegistryListener extends DefaultRegistryListener {

    	/* start of optimization */
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            // deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            
        	// should log
            // "Discovery failed of '" + device.getDisplayString() + "': " + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"));
            
        	// deviceRemoved(device);
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
        			
        			// _Ctrl.show("device.getType().toString() :  " + device.getType().toString());
        			
        			if (device.getType().toString().equals(deviceTypeMediaServer))
        			{
        				_Ctrl.show("MediaServer " + device.getServices().length  + " services");
        				
        				browse(device);
        				
//        				Service service = device.findService(new UDAServiceId("ContentDirectory"));
//        				Action action = service.getAction("Browse");
//        				browse2(action, upnpService);
        				
        				
//        				for (Service service : device.getServices())
//        				{
//        					// _Ctrl.show("MediaServer : " + service.toString());
//        					Log.i(LOG_TAG, "service : " + service.toString());
//        					
//        					for (Action action : service.getActions())
//            				{
//        						Log.i(LOG_TAG, "action : " + action.toString());
//        						
//        						if (action.toString().equals("Browse"))
//        						{
//        							//action.
//        							
//        						}
//            				}
//        				}
        			}
        			
		            // _Ctrl.show("deviceAdded : " + device.toString());
		            int position = deviceList.indexOf(device);
		            if (position >= 0) {
		                // Device already in the list, re-set new value at same position
		                deviceList.remove(device);
		                deviceList.add(position, device);
		            } else {
		                deviceList.add(device);
		            }
		            
		            // TODO do it in a better way
		            _Ctrl.getJavascriptWrapper().sendDeviceList(deviceList);
        		}
        	});
        }

        public void deviceRemoved(final Device device) {
        	
        	_Ctrl.runOnUiThread(new Runnable() {
        		public void run() {
		        	_Ctrl.show("deviceRemoved : " + device.toString());
		        	deviceList.remove(device);
		        	
		        	// TODO do it in a better way
		        	_Ctrl.getJavascriptWrapper().sendDeviceList(deviceList);
        		}
        	});
        }
    }
	
}
