package com.example.com.exampe.changetheme;

import android.app.Activity;
import android.util.Log;

public class Utils {
	
	public static String SIZE="";
	 public static boolean settingChanged=false;
	 public static String THEME="";

	public static void setThemeToActivity(Activity act )
	 {
	  
	  try {
	   

			if (Utils.SIZE.equalsIgnoreCase("LARGE"))
			{
			    act.setTheme(R.style.Theme_LargeText);
			    Log.d(" ", "Theme Large txt size is to be is applied.");
			}
			 if (Utils.SIZE.equalsIgnoreCase("SMALL"))
			{
			    act.setTheme(R.style.Theme_SmallText);
			    Log.d(" ", "Theme Small text Size is to be is applied.");
			}
			 
			 if(Utils.SIZE.equalsIgnoreCase("DEFAULT"))
			 {
				 act.setTheme(R.style.Theme_DefaultText);
				 Log.d("", "theme default text size is applied.");
			 }
			 
			 if(Utils.THEME.equalsIgnoreCase("defaultTheme"))
			 {
				 act.setTheme(R.style.Theme_DefaultTheme);
				 Log.d("", "Default theme is to be applied.");
			 }


			 if(Utils.THEME.equalsIgnoreCase("Gray"))
			 {
				 act.setTheme(R.style.Theme_Gray);
				 Log.d("", "gray theme is to be applied.");
			 }

			 if(Utils.THEME.equalsIgnoreCase("Radial"))
			 {
				 act.setTheme(R.style.Theme_Radial);
				 Log.d("", "radial theme is to be applied.");
			 }
	     
	  
	  }
	  catch (Exception e) {
		e.printStackTrace();
	}
	  
	 }
}