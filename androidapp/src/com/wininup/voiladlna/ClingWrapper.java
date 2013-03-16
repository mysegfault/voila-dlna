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

import java.io.IOException;
import java.net.URI;
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
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;


public class ClingWrapper {

	
	
	/**
	 * @todo  find a better way to specify Type
	 * */
	private final static String deviceTypeMediaServer = "urn:schemas-upnp-org:device:MediaServer:1";
	private final static String deviceTypeMediaRenderer = "urn:schemas-upnp-org:device:MediaRenderer:1";	
	
	private Controller _Ctrl;
	
	private static final String LOG_TAG = "UPNP ClingWrapper";

	private ServiceId avServiceId = new UDAServiceId("AVTransport");
	private ServiceId contentDirectoryServiceId = new UDAServiceId("ContentDirectory");

	
	private AndroidUpnpService upnpService;
	private List<Device> deviceList;
	private Device mediaRenderer;
	
	private BrowseRegistryListener registryListener = new BrowseRegistryListener();
	private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;
            // Refresh the list with all known devices
            deviceList.clear();
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.updateDeviceList(device, false);
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
	
	
	
	private Device findDevice(String UDN) {

		for (Device device : deviceList)
		{
			if (device.getIdentity().getUdn().toString().equals(UDN))
				return device;
		}
		return null;
	}

	/**
	 * 
	 * @param UDN  Unique Device Name to specify device
	 * @param path to browse
	 */
	public void browse(String UDN, String path)
	{
		Log.i(LOG_TAG, "browsing " + UDN + " at " + path);
		browse(findDevice(UDN), path);
	}
	
	/*
	 * @param device	device to browse
	 * @param id		"0" is root
	 */
	private void browse(final Device device, final String id){
		
		if (device != null)
		{
			Log.i(LOG_TAG, "browsing id " + id + " on " + device.getDisplayString() + " " + device.getIdentity().getUdn().toString());
			try {
				new Thread(new Runnable() {
					@Override
					public void run() {
							Service service = device.findService(contentDirectoryServiceId);
							ActionCallback simpleBrowseAction = new Browse(service, id, BrowseFlag.DIRECT_CHILDREN) {

								@Override
							    public void received(ActionInvocation actionInvocation, DIDLContent didl) {
					
									List<Container> containers = didl.getContainers();
									List<Item> items = didl.getItems();
									
									// didl.
									
									Log.i(LOG_TAG, "found " + containers.size() + " containers and " + items.size() + " items" );
									
									for (Item item : items) {
										
										String uri = null;
										
										Log.i(LOG_TAG, item.getTitle() + " (" + item.getId() + ")");
										for (Res ressource : item.getResources()) {
											uri = ressource.getValue();
											
											Log.i(LOG_TAG, "	" + ressource.getValue());
										}
										
										
										// TEST
										// [www.Cpasbien.com] s8-rouille-xvid.cd2.avi (13)   http://192.168.1.3:49152/content/media/object_id/13/res_id/0/ext/file.avi
										//
										// 01 Adieu tristesse.mp3
										//
										if (item.getTitle().equals("01 Adieu tristesse.mp3")) {
										
											if (mediaRenderer != null && uri != null) {
												//sendToDMR(mediaRenderer, uri, "");
												//play(mediaRenderer, "0");
												
												playLocally(Uri.parse(uri));
												
											}
											
										}
										
									}
									
									// browse all
									for (Container container : containers) {										
										browse( device, container.getId());
									}
							    }
					
							    @Override
							    public void updateStatus(Status status) {
							        // Called before and after loading the DIDL content
							    }
					
							    @Override
							    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
							        // Something wasn't right...
							    	Log.d(LOG_TAG, "browse failure " + defaultMsg);
							    }
							};
							
							// starting browse request
							simpleBrowseAction.setControlPoint(upnpService.getControlPoint()); 
							simpleBrowseAction.run();
					}
				}).start();
			}
			catch (Exception ee) {
				Log.d(LOG_TAG, ee.getMessage(), ee);
			}
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
	
	 public void sendToDMR(Device device, String uri, String metadata) {
		 
		 Service avService = device.findService(avServiceId);
		 
         if (avService != null && upnpService != null) {
                 ActionInvocation actionInvocation = new SetAVTransportURI(avService, "0", uri, metadata);

                 upnpService.getControlPoint().execute(
                                 new ActionCallback(actionInvocation) {

                                         @Override
                                         public void failure(ActionInvocation arg0,
                                                         UpnpResponse arg1, String arg2) {

                                                 Log.e(LOG_TAG, "Send UIR to DMR fail");
                                                 Log.e(LOG_TAG, arg2);
                                         }

                                         @Override
                                         public void success(ActionInvocation arg0) {
                                        	 Log.i(LOG_TAG, "Send UIR to DMR success");
                                         }
                                 });
	         }
	 }

	 public void play(Device device, String instanceID) {
		 Service avService = device.findService(avServiceId);
		 
         if (avService != null && upnpService != null) {
                 ActionInvocation actionInvocation = new Play(avService, instanceID);

                 upnpService.getControlPoint().execute(
                     new ActionCallback(actionInvocation) {

                             @Override
                             public void failure(ActionInvocation arg0,
                                             UpnpResponse arg1, String arg2) {
                                     Log.e(LOG_TAG, "play fail" + arg0.getFailure());
                             }

                             @Override
                             public void success(ActionInvocation arg0) {
                                     Log.i(LOG_TAG, "play success");
                             }
                     });
	         }
	 }
	 
     public void stop(Device device) {
    	 
    	 Service avService = device.findService(avServiceId);
    	 
         if (avService != null && upnpService != null) {
                 ActionInvocation<Service> actionInvocation = new Stop(avService);
                 upnpService.getControlPoint().execute(
                     new ActionCallback(actionInvocation) {

                             @Override
                             public void failure(ActionInvocation arg0,
                                             UpnpResponse arg1, String arg2) {
                                     Log.e(LOG_TAG, arg0.getAction().getName() + " fail");
                             }

                             @Override
                             public void success(ActionInvocation arg0) {
                                     Log.e(LOG_TAG, arg0.getAction().getName() + " success");
                             }
                     });
         }
 }


     void playLocally(final Uri uri) {
    	 
    	// running in UI thread
     	_Ctrl.runOnUiThread(new Runnable() {
				public void run() {
    	 
    	 	MediaPlayer player = MediaPlayer.create(_Ctrl.getContext(), uri);
    	    player.setOnPreparedListener(new OnPreparedListener() { 
    	        @Override
    	        public void onPrepared(MediaPlayer mp) {
    	            mp.start();
    	        }
    	    });
    	    
    	    try {
				player.prepareAsync();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
    	    
				}
     	});

     }
     
	
  // class SetAVTransportURI extends ActionInvocation {

    
    /**
     * 
     * receives upnp discovery service event in service thread context
     * 
     * */
    protected class BrowseRegistryListener extends DefaultRegistryListener {

    	/* start of optimization (disabled)
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            // deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            // "Discovery failed of '" + device.getDisplayString() + "': " + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"));
        	// deviceRemoved(device);
        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        	Log.i(LOG_TAG, "remoteDeviceAdded : " + device.toString() + " " + device.getIdentity().getUdn().toString());
        	updateDeviceList(device, false);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        	Log.i(LOG_TAG, "remoteDeviceRemoved : " + device.toString() + " " + device.getIdentity().getUdn().toString());
        	updateDeviceList(device, true);
        }

        /* local services
        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }
        */

        public void updateDeviceList(final Device device, final boolean toRemove) {
        	
        	// running in UI thread
        	_Ctrl.runOnUiThread(new Runnable() {
				public void run() {
        			
        			if (device.getType().toString().equals(deviceTypeMediaServer))
        			{
        				browse(device, "0");
        			}
        			else if (device.getType().toString().equals(deviceTypeMediaRenderer))
        			{
        				mediaRenderer = device;
        			}
        			
        			
        			if (toRemove) {
        				deviceList.remove(device);
        			}
        			else {// to add        			
			            int position = deviceList.indexOf(device);
			            if (position >= 0) {
			                // Device already in the list, re-set new value at same position
			                deviceList.remove(device);
			                deviceList.add(position, device);
			            } else {
			                deviceList.add(device);
			            }
        			}
		            // TODO do it in a better way
		            _Ctrl.getJavascriptWrapper().sendDeviceList(deviceList);
        		}
        	});
        }
    }
    
    class SetAVTransportURI extends ActionInvocation {
        public SetAVTransportURI(Service service, String instanceID,
                        String uri, String metadata) {
                super(service.getAction("SetAVTransportURI"));
                try {
                        setInput("CurrentURIMetaData", metadata);
                        setInput("InstanceID", new UnsignedIntegerFourBytes(instanceID));
                        setInput("CurrentURI", uri);
                        Log.i(LOG_TAG, "URI value: " + uri);
                } catch (Exception ex) {
                        ex.printStackTrace();
                }
	        }
	}
    

    class Play extends ActionInvocation {
            public Play(Service service, String instanceID) {
                    super(service.getAction("Play"));
                    try {
                            setInput("InstanceID", new UnsignedIntegerFourBytes(instanceID));
                            setInput("Speed", "1");
                    } catch (Exception ex) {
                            ex.printStackTrace();
                    }
            }
    }

    class Stop extends ActionInvocation {
            public Stop(Service service) {
                    super(service.getAction("Stop"));
                    try {
                            setInput("InstanceID", new UnsignedIntegerFourBytes(0));
                    } catch (Exception ex) {
                            ex.printStackTrace();
                    }
            }
    }


}
