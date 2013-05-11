
var Gui = {
	ids: [],
	addDevice: function(infos) {

//		App.debug('id: ' + infos.id + ' => ' + (this.ids[infos.id] ? 'Y' : 'N'));

		if (!this.ids[infos.id]) {
			this.ids[infos.id] = this.ids.length + 1;
			infos.idx = this.ids[infos.id];
			$('#devices').append(this._createDeviceIcon(infos));
		}
		else {
			infos.idx = this.ids[infos.id];
			this._setDeviceType(infos);
		}
	},
	_setDeviceType: function(infos) {
		var type = this._parseDeviceType(infos.type);
		$('#' + infos.idx).addClass('device-type-' + type);
	},
	_parseDeviceType: function(upnpType) {
		var type = 'unknown';
		if (upnpType.match(/MediaServer/)) {
			type = 'server';
		}
		if (upnpType.match(/MediaRenderer/)) {
			type = 'rendered';
		}
		if (upnpType.match(/InternetGatewayDevice/)) {
			type = 'gateway';
		}
		return type;
	},
	_createDeviceIcon: function(infos) {
		var html = '';
		html += '<span class="device device-type-#TYPE#" id="device-id-#ID#">#NAME#</span>';

		var type = this._parseDeviceType(infos.type);
		App.debug(type);
		html = html.replace('#ID#', infos.idx || 'Unknown');
		html = html.replace('#NAME#', infos.name || 'Unknown');
		html = html.replace('#TYPE#', type);
		return html;
	}
};
