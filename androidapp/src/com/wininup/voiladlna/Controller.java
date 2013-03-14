package com.wininup.voiladlna;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class Controller {

	static Controller _instance;
	private Context _context;
	private Toast 	_toast; // must we simply call makeText each time we need it
	private Activity mainActivity;
	private Dlna	javascriptWrapper;
	private ClingWrapper clingwrapper;
	
	/**
	 * show the text in a toast
	 * 
	 * @param text to show
	 */
	public void show(String text) {
		_toast.setText(text);
		_toast.show();
	}
	
	/**
	 * 
	 * @return App context
	 */
	public Context getContext() {
		return _context;
	}
	
	/**
	 * constructor of the Controller singleton
	 * @param context
	 */
	private Controller(Context context) {
		_context = context;
		_toast = Toast.makeText(_context, "", Toast.LENGTH_LONG);
		// clingwrapper = new ClingWrapper();
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	static public Controller getInstance(Context context) {
		if (_instance == null && context != null)
			_instance = new Controller(context);
			
		return _instance;
	}

	/**
	 * @param the main and lonely activity
	 * */
	public void setActivity(VoilaDLNA voilaDLNA) {

		 mainActivity =	voilaDLNA;
		
	}

	/**
	 * @return the javascriptWrapper
	 */
	public Dlna getJavascriptWrapper() {
		return javascriptWrapper;
	}

	/**
	 * @param javascriptWrapper the javascriptWrapper to set
	 */
	public void setJavascriptWrapper(Dlna javascriptWrapper) {
		this.javascriptWrapper = javascriptWrapper;
	}
	
	/**
	 * @return the clingwrapper
	 */
	public ClingWrapper getClingwrapper() {
		return clingwrapper;
	}
	
	/**
	 * @param the clingwrapper
	 */
	public void setClingWrapper(ClingWrapper clingwrapper) {
		this.clingwrapper = clingwrapper;
	}

	public void runOnUiThread(Runnable runnable) {
		mainActivity.runOnUiThread(runnable);		
	}
	
	/**
	 * Change to force eclipse to recompile after non java (HTML/JS) modifications
	 **/
    private void Dummy() {
    	int bitches = 5;
    	bitches++;
    	bitches++;	
    }

}
