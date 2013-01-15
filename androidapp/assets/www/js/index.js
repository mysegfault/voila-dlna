/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var App = function() {
	this.isAndroidEnv = window.device;
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
	var parentElement = document.getElementById('deviceready');
	var listeningElement = parentElement.querySelector('.listening');
	var receivedElement = parentElement.querySelector('.received');

	listeningElement.setAttribute('style', 'display:none;');
	receivedElement.setAttribute('style', 'display:block;');
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
