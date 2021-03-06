package dk.jens.backup;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class ScheduleService extends Service
implements BackupRestoreHelper.OnBackupRestoreListener
{
    static final String TAG = OAndBackup.TAG;
    static final int ID = 2;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        int id = intent.getIntExtra("dk.jens.backup.schedule_id", -1);
        if(id >= 0) {
            SharedPreferences prefs;
            SharedPreferences.Editor edit;
            HandleAlarms handleAlarms = new HandleAlarms(this);
            HandleScheduledBackups handleScheduledBackups = new HandleScheduledBackups(this);
            handleScheduledBackups.setOnBackupListener(this);
            prefs = getSharedPreferences("schedules", 0);
            edit = prefs.edit();
            long timeUntilNextEvent = handleAlarms.timeUntilNextEvent(
                prefs.getInt("repeatTime" + id, 0),
                prefs.getInt("hourOfDay" + id, 0));
            edit.putLong("timeUntilNextEvent" + id, timeUntilNextEvent);
            edit.putLong("timePlaced" + id, System.currentTimeMillis());
            edit.commit();
            Log.i(TAG, getString(R.string.sched_startingbackup));
            int mode = prefs.getInt("scheduleMode" + id, 1);
            int subMode = prefs.getInt("scheduleSubMode" + id, 2);
            boolean excludeSystem = prefs.getBoolean("excludeSystem" + id, false);
            handleScheduledBackups.initiateBackup(id, mode, subMode + 1,
                excludeSystem); // add one to submode to have it correspond to AppInfo.MODE_*
        } else {
            Log.e(TAG, "got id: " + id + " from " + intent.toString());
        }

        return Service.START_NOT_STICKY;
    }
    @Override
    public void onCreate()
    {
        // build an empty notification for the service since progress
        // notifications are handled elsewhere
        Notification notification = new Notification.Builder(this).build();
        startForeground(ID, notification);
    }
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    @Override
    public void onDestroy()
    {
        stopForeground(true);
    }
    @Override
    public void onBackupRestoreDone()
    {
        stopSelf();
    }
}
