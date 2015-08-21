package klappztech.com.ailrsa;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class FullscreenActivity extends Activity {


    WebView myWebView;
    public static final String WEB_URL = "http://ailrsaapp.blogspot.in/", EXTRA_MODE_INT = "MODE";
    ProgressBar progress;
    ImageButton showBM,saveBM;
    ImageView loadError;
    boolean isFirstTime=false,isError=false;
    TextView progressPercent,txtMode;
    int globalMode=0;//  0:offline, 1:online

    DBAdapter myDb ;
    private Animation twistAnim;
    private BroadcastReceiver broadcastURLReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //fullscreen
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);   //new
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().hide();                                   //new
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);
        isFirstTime=true;

        //ads
        final AdView mAdView = (AdView) findViewById(R.id.adView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //database
        openDB();

        //my views
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progressPercent = (TextView) findViewById(R.id.progressNum);
        //loadError = (ImageView) findViewById(R.id.imageViewBg);
        myWebView = (WebView) findViewById(R.id.webview);
        //buttons
        showBM = (ImageButton) findViewById(R.id.buttonShowBM);
        saveBM = (ImageButton) findViewById(R.id.buttonSave);
        //text
        txtMode = (TextView) findViewById(R.id.textMode);

        // hide loader
        showProgressBar(false);
        showError(false);

        //******************** Broadcast reciever *********************
        broadcastURLReceiver =  new BroadcastReceiver() {
            //@SuppressLint("NewApi")
            @Override
            public void onReceive(Context context, Intent intent) {

                if (myWebView.getVisibility() != View.VISIBLE) {
                    showError(false);

                }
                String url = intent.getStringExtra(bookmarks.EXTRA_URL_TEXT);
                String mode = intent.getStringExtra(Splash.EXTRA_MODE_INT);
                if(url!=null) {
                    myWebView.loadUrl(url);
                    Log.e("mahc", "==========Received broadcast url" + url);
                }

                if(mode != null) {
                    if(mode == "online") {
                        //online
                        globalMode = 1;
                        Toast.makeText(getApplicationContext(), "ONLINE MODE", Toast.LENGTH_SHORT).show();
                        txtMode.setText("online");
                        myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                        openWebUrl(WEB_URL+"?m=1");
                    } else if(mode == "offline") {
                        //offline
                        globalMode = 0;
                        Toast.makeText(getApplicationContext(), "OFFLINE MODE", Toast.LENGTH_SHORT).show();
                        txtMode.setText("offline");
                        myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                        openWebUrl(WEB_URL+"?m=1");
                    }
                }



            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastURLReceiver, new IntentFilter(bookmarks.ACTION_BROADCAST));
        //******************** Broadcast reciever ends*********************



        //******************** Open Splash*********************

        // start main
        startActivity(new Intent("com.klappzetch.ailrsa.CLEARSCREEN"));



        //button onclicklisterenters
        showBM.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //load bookmarks
                startActivity(new Intent("com.klappzetch.ailrsa.BOOKMARKS"));
            }
        });

        saveBM.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //load bookmarks
                Toast.makeText(getApplicationContext(), "save button", Toast.LENGTH_SHORT).show();

                long newId = myDb.insertRow(myWebView.getTitle(), myWebView.getUrl(), 1);

                // animate bookmark button
                twistAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.bookmark_highlight);
                showBM.startAnimation(twistAnim);

                // Query for the record we just added.
                // Use the ID:
                Cursor cursor = myDb.getRow(newId);
            }
        });

    }

    private void openWebUrl(String url) {

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        myWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        myWebView.loadUrl(url);
        // for progress bar
        final Activity activity = this;
        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%

                progressPercent.setText(Integer.toString(progress) + "%");
            }
        });
        //page opens in the same view
        myWebView.setWebViewClient(new MyWebViewClient());
    }

    public void showError(boolean status) {
        //ImageView loadError = (ImageView) findViewById(R.id.imageViewBg);
        WebView myWebView = (WebView) findViewById(R.id.webview);
        TextView ErrorMsg = (TextView) findViewById(R.id.textViewError);

        if(status) {
            myWebView.setVisibility(View.GONE);
            //loadError.setVisibility(View.VISIBLE);
            ErrorMsg.setText("Please check your internet connection!");
            ErrorMsg.setVisibility(View.VISIBLE);
        } else {
            myWebView.setVisibility(View.VISIBLE);
            //loadError.setVisibility(View.GONE);
            ErrorMsg.setText("0%");
            ErrorMsg.setVisibility(View.GONE);
        }


    }

    private void showProgressBar(boolean status) {
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progressPercent = (TextView) findViewById(R.id.progressNum);
        if(status) {
            progress.setVisibility(View.VISIBLE);
            progressPercent.setVisibility(View.VISIBLE);
        } else {
            progress.setVisibility(View.GONE);
            progressPercent.setVisibility(View.GONE);
        }

    }

    private void openDB() {

        myDb = new DBAdapter(this);
        myDb.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastURLReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastURLReceiver);
            broadcastURLReceiver = null;
        }
        closeDB();
    }

    private void closeDB() {
        myDb.close();
    }

    // buttons
    public void webBack(View v) {
        if ( myWebView.canGoBack()) {
            myWebView.goBack();
        }
    }
    public void webForward(View v) {
        if (myWebView.canGoForward()) {
            myWebView.goForward();
        }
    }

    public void webClearCache(View v) {
        Toast.makeText(getApplicationContext(), "Clear Cache", Toast.LENGTH_SHORT).show();
            myWebView.clearCache(true);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bookmarks, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            webBack(myWebView);
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }


    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("mahc", "URL: "+ url);
            if (Uri.parse(url).getHost().equals(Uri.parse(WEB_URL).getHost())) {
                // This is my web site, so do not override; let my WebView load the page

                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);

            Log.e("mahc", "open Outside!!");
            Toast.makeText(getApplicationContext(), Uri.parse(url).getHost() + " , External Link", Toast.LENGTH_SHORT).show();

            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressPercent.setText("0%");
            if(isFirstTime) {
                myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                isFirstTime =  false;
            }
            showProgressBar(false);
            if(isError == false) {
                showError(false);
            }

        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            showProgressBar(true);
            isError = false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            isError = true;
            showError(true);

        }
    }

}
