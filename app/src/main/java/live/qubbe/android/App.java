package live.qubbe.android;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/ubuntu.regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}

