var App = function() {
	this.isAndroidEnv = (window.location.search === '?_phonegap=true');
	this.initialize();
	window.App = this;
};
App.prototype.initialize = function() {
	this.debug("Start App - " + (this.isAndroidEnv ? 'Android' : 'Browser') + ' detected');
	var readyEvent = this.isAndroidEnv ? 'deviceready' : 'DOMContentLoaded';
	document.addEventListener(readyEvent, this.__onDeviceReady, false);

	if (this.isAndroidEnv) {
		var debugElement = document.getElementById('debug');
		debugElement.style.display = "block";
	}
};
App.prototype.debug = function(text) {
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
	that.initNativeCode();
	that.MainApp();
};
App.prototype.initNativeCode = function() {
	if (!this.isAndroidEnv) {
		return;
	}
	window.echo = function(str, callback) {
		cordova.exec(callback, function(err) {
			callback('Nothing to echo.');
		}, "Dlna", "echo", [str]);
	};
	this.debug('Native code plugin "Dlna" registred.');
};
App.prototype.UpdateReadyGui = function() {
	$('#deviceready').text('Connected!');
};
App.prototype.MainApp = function() {
	this.debug('-- Run Main Application Code --');
	var that = this;

	this.UpdateReadyGui();

	try {
		if (this.isAndroidEnv) {
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
