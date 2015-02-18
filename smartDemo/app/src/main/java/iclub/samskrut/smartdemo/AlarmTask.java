package iclub.samskrut.smartdemo;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmTask implements Runnable
{
    private final Calendar date;
    private final AlarmManager am;
    private final Context context;
    private int uid;

    public AlarmTask(Context context, Calendar date, int uid) {
        this.context = context;
        this.uid=uid;
        this.am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.date = date;
    }

    @Override
    public void run()
    {
        Intent intent = new Intent(context, NotifyService.class);
        intent.putExtra(NotifyService.INTENT_NOTIFY, true);
        PendingIntent pendingIntent = PendingIntent.getService(context, uid, intent, 0);
        am.set(AlarmManager.RTC, date.getTimeInMillis(), pendingIntent);
    }
}