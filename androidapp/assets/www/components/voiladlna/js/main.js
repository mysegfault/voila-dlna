
(function() {
	var deps = [
		"/components/jquery/jquery.js",
		"/components/phonegap/lib/android/cordova-2.5.0.js",
		"/components/voiladlna/js/voiladlna.js"
	];
	require(deps, function() {
		new App();
	});
})();
