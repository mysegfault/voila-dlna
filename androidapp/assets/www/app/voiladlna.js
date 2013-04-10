var App = function() {
	this.isAndroidEnv = (window.location.search === '?_phonegap=true');
	this.debug("App Constructor : " + window.location.search);
	this.initialize();
	window.App = this;
};
App.prototype.initialize = function() {
	this.debug("initialize - " + (this.isAndroidEnv ? 'Android' : 'Browser') + ' detected');
		
	var readyEvent = this.isAndroidEnv ? 'deviceready' : 'DOMContentLoaded';
	document.addEventListener(readyEvent, this.__onDeviceReady, false);

	if (this.isAndroidEnv) {
		var debugElement = document.getElementById('debug');
		debugElement.style.display = "block";
	}
};
App.prototype.debug = function(text) {
	
	console.log("Debug### " + text);
	
	if (this.isAndroidEnv) {
		var debugElement = document.getElementById('debug');
		debugElement.value = (debugElement.value + "\n" + text);
	}
	else {
		console.log("Debug### " + text);
	}
};
// "__" prefix means that this method is called in the "window" context
App.prototype.__onDeviceReady = function() {
	
	var that = window.App;
	that.debug("__onDeviceReady");
	that.initNativeCode();
	that.MainApp();
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
	cordova.exec(this._onBrowseDevice /* async no need to callback */, this._onCordovaError, "Dlna", "browseDevice", [deviceUdn, containerId]);
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
		
		var that = window.App;
		var devicesStr = JSON.stringify(devices , null, 4);
		
		that.debug("_onDevicesChanged : " + devicesStr);
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
		var containerStr = JSON.stringify(container , null, 4);
		
		that.debug("_onBrowseDevice : " + containerStr);
		
	}
};


/**
 *	  Error from cordova plugin
 * */
App.prototype._onCordovaError = function(e) {
	var that = window.App;
	that.debug('_onError : ' + e);
};

App.prototype.UpdateReadyGui = function() {
	try {
		$('#deviceready').text('Connected ! jquery ');
		var elem = document.getElementById('deviceready');
		elem.innerHTML = 'Connected innerHTML !';
	}
	catch (err) {
		this.debug("UpdateReadyGui error : " + err.message );
	}
};
App.prototype.MainApp = function() {
	this.debug('-- Run Main Application Code --');
	var that = this;

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
