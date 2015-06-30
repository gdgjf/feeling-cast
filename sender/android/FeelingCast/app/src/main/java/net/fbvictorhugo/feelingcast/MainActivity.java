package net.fbvictorhugo.feelingcast;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "> GDG >>>";
    private final String NAMESPACE = "urn:x-cast:net.fbvictorhugo.feelingcast";

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mCastDevice;
    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient.ConnectionCallbacks mGoogleConnectionCallbacks;
    private GoogleApiClient.OnConnectionFailedListener mGoogleConnectionFailedListener;

    Toolbar mToolbar;
    EditText editText;
    Button btnPositivo;
    Button btnNegativo;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        editText = (EditText) findViewById(R.id.main_edittext);
        btnPositivo = (Button) findViewById(R.id.main_btn_pos);
        btnNegativo = (Button) findViewById(R.id.main_btn_neg);
        textView = (TextView) findViewById(R.id.main_text_view);

        btnPositivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction("positive");
            }
        });

        btnNegativo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction("negative");
            }
        });

        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder().
                addControlCategory(CastMediaControlIntent.categoryForCast(getResources().getString(R.string.cast_app_id))).build();
        mMediaRouterCallback = new MyMediaRouterCallback();
    }

    private void buttonAction(String choice) {
        try {
            String mes = createJsonData(choice, editText.getText().toString());
            textView.setText(mes);
            sendMessageToCast(mes);

        } catch (Exception e) {
            textView.setText(e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    protected void onStop() {
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onStop();
    }

    // region OptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);

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
    // endregion

    // region My MediaRouter.Callback
    class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
            //  super.onRouteSelected(router, route);
            // Handle the user route selection.
            mCastDevice = CastDevice.getFromBundle(route.getExtras());
            mToolbar.setSubtitle(route.getName());
            prepareGoogleApis();
        }

        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            // super.onRouteAdded(router, route);
            Log.d(TAG, route.getName());
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
            mToolbar.setSubtitle("");
        }
    }

    //endregion

    // region Prepare Goolge APIs

    private void prepareGoogleApis() {
        Cast.Listener castListener = new Cast.Listener() {

            @Override
            public void onApplicationDisconnected(int statusCode) {
                mToolbar.setSubtitle("Tchau " + statusCode);
            }
        };

        // Cast API options
        Cast.CastOptions.Builder castOptionsBuilder = Cast.CastOptions
                .builder(mCastDevice, castListener);

        mGoogleConnectionCallbacks = new GoogleConnectionCallbacks();
        mGoogleConnectionFailedListener = new GoogleConnectionFailedListener();

        // The Cast SDK API’s are invoked using GoogleApiClient
        GoogleApiClient.Builder googleApiBuilder = new GoogleApiClient.Builder(this);
        googleApiBuilder.addApi(Cast.API, castOptionsBuilder.build());
        googleApiBuilder.addConnectionCallbacks(mGoogleConnectionCallbacks);
        googleApiBuilder.addOnConnectionFailedListener(mGoogleConnectionFailedListener);
        mGoogleApiClient = googleApiBuilder.build();

        // connect
        mGoogleApiClient.connect();
    }

    // endregion

    // region Google Callbacks

    private class GoogleConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle bundle) {

            Cast.CastApi.launchApplication(mGoogleApiClient, getString(R.string.cast_app_id)).
                    setResultCallback(castAppConnectionResultCallback);
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    }

    public class GoogleConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }
    }
    // endregion

    // region Cast Callbacks

    ResultCallback<Cast.ApplicationConnectionResult> castAppConnectionResultCallback = new ResultCallback<Cast.ApplicationConnectionResult>() {
        @Override
        public void onResult(Cast.ApplicationConnectionResult result) {

            if (result.getStatus().isSuccess()) {
                try {
                    Cast.CastApi.setMessageReceivedCallbacks(mGoogleApiClient,
                            NAMESPACE, new CastMessageReceivedCallback());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    class CastMessageReceivedCallback implements Cast.MessageReceivedCallback {

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            Log.d(TAG, "from " + castDevice.getFriendlyName() + " : " + message);
        }
    }

    // endregion

    // region Message to Cast
    void sendMessageToCast(final String message) {
        try {
            Cast.CastApi.sendMessage(mGoogleApiClient, NAMESPACE, message)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status result) {
                            if (result.isSuccess()) {
                                Log.d("Cast.sendMessage", message);
                            }
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //endregion

    String createJsonData(String choice, String message) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("choice", choice);
        json.put("message", message);
        return json.toString();
    }

}
