package iclub.samskrut.smartdemo;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;

public class NotifyService extends Service {

    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }

    public static final String INTENT_NOTIFY = "reminder.iclub.com.remindme.INTENT_NOTIFY";

    @Override
    public void onCreate(){}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getBooleanExtra(INTENT_NOTIFY, false)){
            deletePidFromDatabase();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new ServiceBinder();

    public void deletePidFromDatabase(){
        SQLiteDatabase db;
        db=openOrCreateDatabase("smartdemo.db",SQLiteDatabase.CREATE_IF_NECESSARY, null);
        db.execSQL("DELETE FROM previous_session;");
    }
}