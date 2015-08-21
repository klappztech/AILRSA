package klappztech.com.ailrsa;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;


public class Splash extends Activity {

    public static final String  ACTION_BROADCAST = "URL_BROADCAST", EXTRA_MODE_INT = "MODE";
    Button btnOffline,btnOnline;
    Thread logTimer;
    ProgressBar loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fullscreen
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);   //new
        getActionBar().hide();                                   //new
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        // views
        btnOffline =  (Button) findViewById(R.id.buttonOffline);
        btnOnline  =  (Button) findViewById(R.id.buttonOnline);
        loader  = (ProgressBar) findViewById(R.id.progressBar2);

        // hide buttons
        btnOffline.setVisibility(View.GONE);
        btnOnline.setVisibility(View.GONE);
        loader.setVisibility(View.INVISIBLE);


        if(isNetworkAvailable())  {
            // show buttons
            btnOffline.setVisibility(View.VISIBLE);
            btnOnline.setVisibility(View.VISIBLE);



        } else {
            //show splash and goto offline mode
            loader.setVisibility(View.VISIBLE);
            // thread for delay
            logTimer = new Thread(){
                public void run(){
                    try{
                        sleep(3000);
                        sendBroadcastMessage("offline");
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        finish();
                    }
                }
            };
            logTimer.start();

        }



        btnOffline.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //load bookmarks
                sendBroadcastMessage("offline");
            }
        });

        btnOnline.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //load bookmarks
                sendBroadcastMessage("online");
            }
        });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendBroadcastMessage(String mode) {


        //send broadcast
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_MODE_INT, mode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        Log.e("mahc", "==========sent broadcast" + mode);

        // start main
        //startActivity(new Intent("android.intent.action.MAIN"));
        // close this activity
        this.finish();
    }
}
