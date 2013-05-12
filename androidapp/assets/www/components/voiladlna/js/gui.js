
var Gui = {
	ids: [],
	idxs: 1,
	addDevice: function(infos) {
//		App.debug('id: ' + infos.id + ' => ' + (this.ids[infos.id] ? 'Y' : 'N'));
		
		if (!this.ids[infos.id]) {
			this.ids[infos.id] = true;
			infos.idx = this.idxs++;
			$('#devices').append(this._createDeviceIcon(infos));
			this._setDeviceType(infos);
			this._attachDeviceEvents(infos);
		}
	},
	_setDeviceType: function(infos) {
		var type = this._parseDeviceType(infos.type);
		$('#device-id-' + infos.idx).addClass('device-type-' + type);
	},
	_attachDeviceEvents: function(infos) {
		App.debug('_attachDeviceEvents: ' + infos.name);
		var $device = $('#device-id-' + infos.idx);

		$device.on('touchstart', function() {
			$(this).addClass('touched');
			App.browse(infos.id, '0');
		}).on('touchend', function() {
			$(this).removeClass('touched');
		});
	},
	_parseDeviceType: function(upnpType) {
		var type = 'unknown';
		if (upnpType.match(/MediaServer/)) {
			type = 'server';
		}
		if (upnpType.match(/MediaRenderer/)) {
			type = 'renderer';
		}
		if (upnpType.match(/InternetGatewayDevice/)) {
			type = 'gateway';
		}
		return type;
	},
	_createDeviceIcon: function(infos) {
		var html = '<span class="device" id="device-id-#ID#">#NAME#</span>';

		html = html.replace('#ID#', infos.idx || 'Unknown');
		html = html.replace('#NAME#', infos.name || 'Unknown');
		return html;
	}
};
