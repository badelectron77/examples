package de.joesch_it.funfacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class FunFactsActivity extends AppCompatActivity {

    //private final String TAG = FunFactsActivity.class.getSimpleName();
    private TextView mFactTextView;
    public static final String PREF_FILE = "de.joesch-it.funfacts.PREFERENCE_FILE";
    public static final String PREF_KEY_VAL_PREFIX = "VAL_";
    public final Context mContext = FunFactsActivity.this;
    public static SharedPreferences mSharedPref;
    private FactBook mFactBook;
    private ConstraintLayout mMConstraintLayout;
    private Button mShowFactButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fun_facts);

        mSharedPref = mContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

        mFactBook = new FactBook(mContext);
        mMConstraintLayout = (ConstraintLayout) findViewById(R.id.funFactsActivityLayout);
        mFactTextView = (TextView) findViewById(R.id.factTextView);
        mShowFactButton = (Button) findViewById(R.id.showFactButton);

        int color = mFactBook.getColor();
        mMConstraintLayout.setBackgroundColor(color);
        mFactTextView.setText(mFactBook.getFact());
        mShowFactButton.setTextColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(mFactBook.getDarkerColor());
        }

        mShowFactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = mFactBook.getColor();
                mFactTextView.setText(mFactBook.getFact());
                mMConstraintLayout.setBackgroundColor(color);
                mShowFactButton.setTextColor(color);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(mFactBook.getDarkerColor());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences.Editor editor = mSharedPref.edit();
        String[] mFacts = mContext.getResources().getStringArray(R.array.facts);
        for (int i = 0; i < mFacts.length; i++) {
            String key = PREF_KEY_VAL_PREFIX + i;
            if (mSharedPref.contains(key)) {
                editor.remove(key);
                editor.apply();
            }
        }
    }
}
