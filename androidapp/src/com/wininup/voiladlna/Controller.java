package com.wininup.voiladlna;

import android.content.Context;
import android.widget.Toast;

public class Controller {

	static Controller _instance;
	private Context _context;
	private Toast 	_toast;
	
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
}
