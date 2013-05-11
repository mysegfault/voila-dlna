
(function() {
	var now = new Date().getTime();
	var deps = [
		"components/jquery/jquery.js?" + now,
		"components/voiladlna/js/voiladlna.js?" + now,
		"components/voiladlna/js/gui.js?" + now
	];
	require(deps, function() {
		window.App = new App();
		window.App.initialize();
		
		if (window.App.isAndroidEnv) {
			require(["components/phonegap/cordova-2.7.0.js?" + now]);
		}
		
	});
})();
