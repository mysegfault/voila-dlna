var App = function() {
	this.isAndroidEnv = (window.location.search === '?_phonegap=true');
//	this.debug(window.location.search);
//	this.debug("isAndroidEnv : " + this.isAndroidEnv);
};
App.prototype.initialize = function() {
	this.debug("initialize - " + (this.isAndroidEnv ? 'Android' : 'Browser') + ' detected');

	if (this.isAndroidEnv) {
		document.addEventListener('deviceready', this.__onDeviceReady, false);
	}
	else {
		this.__onDeviceReady();
	}

	if (this.isAndroidEnv) {
		$('#debug').removeClass('display-none');
	}
};
App.prototype.debug = function(text) {

	if (this.isAndroidEnv) {
		if (typeof text !== 'string') {
			try {
				text = JSON.stringify(text);
			}
			catch (e) {
			}
		}
		$('#debug').html($('#debug').html() + "<pre>" + text + "</pre>");
	}
	else {
		console.log("# " + text);
	}
};
// "__" prefix means that this method is called in the "window" context
App.prototype.__onDeviceReady = function() {
//	App.debug("__onDeviceReady");
	App.initNativeCode();
	App.MainApp();
};

/**
 * Registers callback to java cordova plugin
 * */
App.prototype.initNativeCode = function() {

	if (!this.isAndroidEnv) {
		return;
	}

	// echo function to test
//	window.echo = function(str, callback) {
//		cordova.exec(callback, function(err) {
//			callback('Nothing to echo.');
//		}, "Dlna", "echo", [str]);
//	};

	// registers to upnp devices changed event and start a discovery
	cordova.exec(this._onDevicesChanged, this._onCordovaError, "Dlna", "registerDeviceDiscovery", []);

	// register browse medias event
	cordova.exec(this._onBrowseDevice, this._onCordovaError, "Dlna", "registerBrowseDevice", []);

	this.debug('Native code plugin "Dlna" registred.');
//	navigator.notification.vibrate(0);
//	navigator.notification.beep(1);
};

/**
 * Browse request
 * */
App.prototype.browse = function(deviceUdn, containerId) {
	// register browse medias event
	cordova.exec(this._onBrowseDevice /* async, no need to callback */, this._onCordovaError, "Dlna", "browseDevice", [deviceUdn, containerId]);
};

/**
 * Play uri request   play file on the remote device specified
 * */
App.prototype.playUri = function(deviceUdn, uri) {
	// register browse medias event
	cordova.exec(this._onBrowseDevice /* async, no need to callback */, this._onCordovaError, "Dlna", "playUri", [deviceUdn, uri]);
};

/**
 * "_" prefix means that this method is called from java cordova plugin
 * 
 * 	call by java when a upnp device has arrived or gone in the local network
 * 
 * @param devices list, JSON object sent by java
 *
 * */
App.prototype._onDevicesChanged = function(devices) {
	App.debug('onDevicesChanged!');

	if (!devices || !devices.networkServices) {
		App.debug('Invalid content for onDevicesChanged.');
		return;
	}

//	App.debug(devices.networkServices);
	for (var i in devices.networkServices) {
		Gui.addDevice(devices.networkServices[i]);
	}
};

/**
 * 	call by java when a upnp browse request has arrived
 * 
 * @param devices list, JSON object sent by java
 *
 * */
App.prototype._onBrowseDevice = function(container) {
	App.debug('onBrowseDevice!');
	
	App.debug(container);

	if (container) {
//		App.debug("_onBrowseDevice : " + container);
	}
};

/**
 *	  Error from cordova plugin
 * */
App.prototype._onCordovaError = function(e) {
	App.debug('_onCordovaError : ' + e);
};

App.prototype.UpdateReadyGui = function() {
	var text = 'You need to run this app in an Android context.';
	if (this.isAndroidEnv) {
		text = 'Connected!';
	}
	$('#deviceready').text(text);
};
App.prototype.MainApp = function() {
	this.debug('-- Run Main Application Code --');

	this.UpdateReadyGui();
	
	// simulate
	if (!this.isAndroidEnv) {
		var data = {
			networkServices: [
				{id: 'a', type: 'MediaServer', name:'MediaServer'},
				{id: 'b', type: 'MediaRenderer', name:'MediaRenderer'},
				{id: 'c', type: 'Gateway', name:'Gateway'}
			]
		};
		App._onDevicesChanged(data);
	}
	
};
