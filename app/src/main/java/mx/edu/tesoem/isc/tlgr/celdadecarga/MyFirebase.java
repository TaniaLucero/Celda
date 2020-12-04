package mx.edu.tesoem.isc.tlgr.celdadecarga;

import com.google.firebase.database.FirebaseDatabase;

class MyFirebase extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}
