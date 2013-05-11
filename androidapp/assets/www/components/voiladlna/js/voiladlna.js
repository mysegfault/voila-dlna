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
		var debugElement = document.getElementById('debug');
		debugElement.value = (debugElement.value + "\n" + text);
	}
	else {
		console.log("# " + text);
	}
};
// "__" prefix means that this method is called in the "window" context
App.prototype.__onDeviceReady = function() {
	App.debug("__onDeviceReady");
	App.initNativeCode();
	App.MainApp();
};

/**
 * Registers callback to java cordova plugin
 * */
App.prototype.initNativeCode = function() {

	this.debug('initNativeCode, this.isAndroidEnv ' + this.isAndroidEnv);

	if (!this.isAndroidEnv) {
		return;
	}

	// echo function to test
	window.echo = function(str, callback) {
		cordova.exec(callback, function(err) {
			callback('Nothing to echo.');
		}, "Dlna", "echo", [str]);
	};

	// registers to upnp devices changed event and start a discovery
	cordova.exec(this._onDevicesChanged, this._onCordovaError, "Dlna", "registerDeviceDiscovery", []);

	// register browse medias event
	cordova.exec(this._onBrowseDevice, this._onCordovaError, "Dlna", "registerBrowseDevice", []);

	this.debug('Native code plugin "Dlna" registred.');
};

/**
 * Browse request
 * */
App.prototype.Browse = function(deviceUdn, containerId) {
	// register browse medias event
	cordova.exec(this._onBrowseDevice /* async, no need to callback */, this._onCordovaError, "Dlna", "browseDevice", [deviceUdn, containerId]);
};

/**
 * Play uri request   play file on the remote device specified
 * */
App.prototype.PlayUri = function(deviceUdn, uri) {
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

	if (devices) {
		var devicesStr = JSON.stringify(devices, null, 4);
		App.debug("_onDevicesChanged : " + devicesStr);
	}
};

/**
 * 	call by java when a upnp browse request has arrived
 * 
 * @param devices list, JSON object sent by java
 *
 * */
App.prototype._onBrowseDevice = function(container) {

	console.log(container);

	if (container) {

		var that = window.App;
		var containerStr = JSON.stringify(container, null, 4);

		that.debug("_onBrowseDevice : " + containerStr);

	}
};


/**
 *	  Error from cordova plugin
 * */
App.prototype._onCordovaError = function(e) {
	var that = window.App;
	that.debug('_onCordovaError : ' + e);
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

	try {
		if (this.isAndroidEnv) {

			this.debug('window.echo("Yahoo !!!" ... ');

			window.echo("Yahoo !!!", function(echoValue) {
				console.log(echoValue);
				that.debug(echoValue);
			});
		}
	}
	catch (err) {
		console.log(err.message);
	}

	try {
		if (this.isAndroidEnv) {
			navigator.notification.beep(1);
		}
	}
	catch (err) {
		console.log(err.message);
	}

	try {
		if (this.isAndroidEnv) {
			navigator.notification.vibrate(0);
		}
	}
	catch (err) {
		console.log(err.message);
	}
};
