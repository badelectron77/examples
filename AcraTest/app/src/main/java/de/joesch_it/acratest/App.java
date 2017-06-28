package de.joesch_it.acratest;

import android.app.Application;
import android.content.Context;


import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(
        formUri = "http://more-tracks.net/report.php")
public class App extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
