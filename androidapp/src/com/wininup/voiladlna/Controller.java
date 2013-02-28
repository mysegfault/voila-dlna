package com.wininup.voiladlna;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class Controller {

	static Controller _instance;
	private Context _context;
	private Toast 	_toast;
	private Activity mainActivity;
	
	public void show(String text)
	{
		_toast.setText(text);
		_toast.show();
	}
	
	public Context getContext()
	{
		return _context;
	}
	
	private Controller(Context context)
	{
		_context = context;
		_toast = Toast.makeText(_context, "hi !", Toast.LENGTH_LONG);
	}
	
	static public Controller getInstance(Context context)
	{
		if (_instance == null && context != null)
			_instance = new Controller(context);
			
		return _instance;
	}

	public void setActivity(VoilaDLNA voilaDLNA) {

		 mainActivity =	voilaDLNA;
		
	}

	public void runOnUiThread(Runnable runnable) {
		mainActivity.runOnUiThread(runnable);		
	}
	
	/**
	 * Change to force eclipse to recompile after non java (HTML/JS) modifications
	 **/
    private void Dummy()
    {
    	int bitches = 5;
    	bitches++;
    	bitches++;	
    }
}
