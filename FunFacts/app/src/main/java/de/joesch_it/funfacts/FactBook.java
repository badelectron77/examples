package de.joesch_it.funfacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.Random;
import static de.joesch_it.funfacts.FunFactsActivity.*;

public class FactBook {

    //private final String TAG = FactBook.class.getSimpleName();
    private String[] mFacts;
    private String[] mScreenColors;
    private String[] mDarkerColors;
    private String mFact = "";
    private int mSize;
    private int mColorIndex;
    private Random random = new Random();

    // Constructor
    public FactBook(Context mContext) {
        mFacts = mContext.getResources().getStringArray(R.array.facts);
        mScreenColors = mContext.getResources().getStringArray(R.array.screenColors);
        mDarkerColors = mContext.getResources().getStringArray(R.array.darkerColors);
        mSize = mFacts.length;
        createArray();
    }

    public int getColor() {
        mColorIndex = random.nextInt(mSize);
        return Color.parseColor(mScreenColors[mColorIndex]);
    }

    public int getDarkerColor() {
        return Color.parseColor(mDarkerColors[mColorIndex]);
    }

    public String getFact() {
        boolean keyFound = false;
        for (int i = 0; i < mSize; i++) {
            // loop through Array from the start
            String prefKey = PREF_KEY_VAL_PREFIX + i;
            if (mSharedPref.contains(prefKey)) {
                mFact = mFacts[getRandomIndex(prefKey)];
                //Log.e(TAG, "########### " + prefKey + " used");
                keyFound = true;
                break;
            }
        }

        if (!keyFound) {
            createArray();
            mFact = mFacts[getRandomIndex(PREF_KEY_VAL_PREFIX + 0)];
            //Log.e("LOG", "########### " + PREF_KEY_VAL_PREFIX + 0 + " used");
        }
        return mFact;
    }

    private void createArray() {
        //Log.e("LOG", " createArray() aufgerufen");
        ArrayList<Integer> list = new ArrayList<>(mSize);
        // fill ArrayList from 1 to mFacts.length
        //Log.e("LOG createArray()", "mSize = " + mSize);
        for (int i = 0; i < mSize; i++) {
            list.add(i);
        }
        Random rand = new Random();
        int i = 0;
        int randomNumber;
        while (list.size() > 0) {
            int index = rand.nextInt(list.size());
            randomNumber = list.remove(index);
            //Log.e("LOG createArray()", PREF_KEY_VAL_PREFIX + i + "->" + randomNumber);
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt(PREF_KEY_VAL_PREFIX + i, randomNumber);
            editor.apply();
            i++;
        }
    }

    private int getRandomIndex(String prefKey) {
        int randomNumber = mSharedPref.getInt(prefKey, 0);
        //Log.e("LOG bottom", PREF_KEY_VAL_PREFIX + i + "->" + randomNumber);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove(prefKey);
        editor.apply();
        return randomNumber;
    }

}
