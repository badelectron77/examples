package com.example.com.exampe.changetheme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class CustomThemeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Utils.setThemeToActivity(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.questions);
		
		
		
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		startActivity(new Intent(getBaseContext(), SettingsTheme.class));
		CustomThemeActivity.this.finish();
	}
}
