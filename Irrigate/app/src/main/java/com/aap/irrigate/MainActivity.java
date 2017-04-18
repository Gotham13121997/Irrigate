package com.aap.irrigate;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView tea;
    GridView gridView;

    ViewGroup mainlayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainlayout = (ViewGroup) findViewById(R.id.main_layout);
        tea=(TextView) findViewById(R.id.weat);
        tea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.asus.weathertime");
                if (intent != null) {
                    // We found the activity now start the activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        gridView = (GridView) findViewById(R.id.grid);
        gridView.setAdapter(new ImageAdapter(this));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if(position==0) {
                    Intent i = new Intent(getApplicationContext(), Main2Activity.class);
                    startActivity(i);
                }
                if(position==1)
                {
                    Intent i = new Intent(getApplicationContext(), Main3Activity.class);
                    startActivity(i);
                }
            }
        });
    }

    /**
     * Registers the AppWidgetHost to listen for updates to any widgets this app
     * has.
     */
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        // Keep all Images in array
        public Integer[] mThumbIds = {
                R.drawable.irri, R.drawable.ferti
        };

        // Constructor
        public ImageAdapter(Context c){
            mContext = c;
        }

        @Override
        public int getCount() {
            return mThumbIds.length;
        }

        @Override
        public Object getItem(int position) {
            return mThumbIds[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(mThumbIds[position]);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new GridView.LayoutParams(330,300));

            return imageView;
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.widget_menu, menu);
        return true;
    }
    @Override

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.A1:

                Toast.makeText(this, "yo", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.A2:

                Toast.makeText(this, "yo2", Toast.LENGTH_SHORT).show();

                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }
    public static class IncomingSms extends BroadcastReceiver {
        // Get the object of SmsManager
        final SmsManager sms = SmsManager.getDefault();
        public void onReceive(Context context, Intent intent) {
            // Retrieves a map of extended data from the intent.
            final Bundle bundle = intent.getExtras();
            try {
                if (bundle != null) {
                    final Object[] pdusObj = (Object[]) bundle.get("pdus");
                    for (int i = 0; i < pdusObj.length; i++) {
                        SmsMessage currentMessage;
                        if (Build.VERSION.SDK_INT >= 19) { //KITKAT
                            SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                            currentMessage = msgs[0];
                        } else {
                            Object pdus[] = (Object[]) bundle.get("pdus");
                            currentMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);
                        }
                        String senderNum = currentMessage.getDisplayOriginatingAddress();
                        String message = currentMessage.getDisplayMessageBody();
                        SharedPreferences pref = context.getSharedPreferences("MyPref", MODE_PRIVATE);
                        if(message.contains(" on"))
                        {
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putLong("ontime",System.currentTimeMillis());
                            editor.apply();
                        }
                        if(message.contains(" off"))
                        {
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putLong("offtime",System.currentTimeMillis());
                            Long off=pref.getLong("offtime",0);
                            Long on=pref.getLong("ontime",0);
                            editor = pref.edit();
                            editor.putLong("difftime",(off-on));
                            Toast.makeText(context,""+(off-on), Toast.LENGTH_LONG).show();
                            editor.apply();
                        }
                        Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                                .setContentTitle("notificaton")
                                .setSmallIcon(R.drawable.add)
                                .setContentText(message)
                                .setPriority(1)
                                .setVibrate(new long[]{0,500})
                                .setAutoCancel(true);
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(1667,mBuilder.build());
                    }
                }

            } catch (Exception e) {
                Log.e("SmsReceiver", "Exception smsReceiver" +e);
            }
        }
    }

}
