package com.example.com.exampe.changetheme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SettingsTheme extends Activity implements View.OnClickListener

{
 /** Called when the activity is first created. */
 @Override
 public void onCreate(Bundle savedInstanceState)
 {
  super.onCreate(savedInstanceState);
  Utils.setThemeToActivity(this);
  setContentView(R.layout.config);

  findViewById(R.id.button1).setOnClickListener(this);
  findViewById(R.id.button2).setOnClickListener(this);
  findViewById(R.id.button3).setOnClickListener(this);
 }

 public void onClick(View v)
 {
  switch (v.getId())
  {
  case R.id.button1:
      Utils.THEME="DEFAULT";
      Utils.settingChanged=true;
      Utils.SIZE="DEFAULT";
      startActivity(new Intent( SettingsTheme.this,  CustomThemeActivity.class));

   break;
  case R.id.button2:
	     Utils.THEME="Gray";
	     Utils.settingChanged=true;
	     Utils.SIZE="SMALL";
	     startActivity(new Intent( SettingsTheme.this,  CustomThemeActivity.class));
   break;
  case R.id.button3:
       Utils.THEME="Radial";
       Utils.settingChanged=true;
       Utils.SIZE="LARGE";
       startActivity(new Intent( SettingsTheme.this,  CustomThemeActivity.class)); 
       break;
  default :
	  break;
  
  }

 }
     public void onBackPressed() {
  // TODO Auto-generated method stub
  super.onBackPressed();
  startActivity(new Intent( SettingsTheme.this,  CustomThemeActivity.class));
  SettingsTheme.this.finish();
  
 }
	
}