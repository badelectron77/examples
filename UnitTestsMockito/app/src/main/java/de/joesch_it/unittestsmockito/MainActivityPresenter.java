package de.joesch_it.unittestsmockito;

import android.graphics.Color;

class MainActivityPresenter {

    private MainActivityView mView;

    MainActivityPresenter(MainActivityView view){
        mView = view;
    }

    void editTextUpdated(String text) {
        mView.changeTextViewText(text);
    }

    void colorSelected(int index) {
        switch (index) {
            case 0:
                mView.changeBackgroundColor(Color.WHITE);
                break;
            case 1:
                mView.changeBackgroundColor(Color.MAGENTA);
                break;
            case 2:
                mView.changeBackgroundColor(Color.GREEN);
                break;
            case 3:
                mView.changeBackgroundColor(Color.CYAN);
                break;
        }
    }

    void launchOtherActivityButtonClicked(Class activity) {
        mView.launchOtherActivity(activity);
    }
}
