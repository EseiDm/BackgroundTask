package org.esei.dm.backgroundtask;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    MyAsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button  =findViewById(R.id.buttonGet);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    launchPostAndroid11();
                else
                    launchPreAndroid11();
            }
        });
    }


    private Boolean isNetworkAvailable(Context activity) {

        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        //controla la disponiblidad de método getActiveNetworkInfo en función del sdk utilizado en ejecución
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }

    }

    class MyAsyncTask extends AsyncTask<Void,Void,String> {

        private Context activity;
        private String result;

        public MyAsyncTask(Context activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return performGet();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(this.activity, "PRE-ANDROID-11:"+result, Toast.LENGTH_LONG).show();
        }
    }

    private String performGet(){

        Boolean connected = isNetworkAvailable(MainActivity.this);
        StringBuilder toret= new StringBuilder("");

        try {
            URL url = new URL( "http://api.geonames.org/timezoneJSON?lat=42.34&lng=-7.86&username=dispositivos_moviles" );
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout( 1000 /* millisegundos */ );
            conn.setConnectTimeout( 1000 /* millisegundos */ );
            conn.setRequestMethod( "GET" );
            conn.setDoInput( true );
            conn.connect();

            // Obtener la respuesta del servidor conn.connect();
            int codigoRespuesta = conn.getResponseCode();

            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            String line = "";
            while( ( line = reader.readLine() ) != null ) {
                toret.append( line );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return toret.toString();
    }


    private void launchPreAndroid11(){
        task = new MyAsyncTask(MainActivity.this);
        task.execute();
    }

    private String launchPostAndroid11() {

        final Executor EXECUTOR = Executors.newSingleThreadExecutor();
        final Handler HANDLER = new Handler( Looper.getMainLooper() );
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                String response = performGet();
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"POST-ANDROID-11:"+response, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        return null;
    }

}