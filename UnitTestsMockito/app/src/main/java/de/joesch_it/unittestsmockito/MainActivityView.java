package de.joesch_it.unittestsmockito;


interface MainActivityView {

    void changeTextViewText(String text);
    void changeBackgroundColor(int color);
    void launchOtherActivity(Class activity);
}
