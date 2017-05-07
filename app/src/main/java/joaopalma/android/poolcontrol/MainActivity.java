package joaopalma.android.poolcontrol;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import joaopalma.android.poolcontrol.db.Contrato;
import joaopalma.android.poolcontrol.db.DB;
import za.co.riggaroo.materialhelptutorial.TutorialItem;
import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity;


public class MainActivity extends AppCompatActivity {//implements SensorEventListener {

    ArrayList<Integer> array_equipamento;
    ArrayList<Double> array_valor;

    ArrayList<Integer> h_array_equipamento;
    ArrayList<Double> h_array_valor;

    DB mDbHelper;
    SQLiteDatabase db;
    boolean atualizar = false;

    private SensorManager sensorManager;
    private long updateTempo;
    MenuItem itemMenuLight;

    HomeFragment hf = HomeFragment.newInstance();
    TempFragment tf = TempFragment.newInstance();
    EnginesFragment ef = EnginesFragment.newInstance();
    LightsFragment lf = LightsFragment.newInstance();
    phcloroFragment pf = phcloroFragment.newInstance();

    private static final int REQUEST_CODE = 1234;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            itemMenuLight = null;
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            boolean disableMenu = sharedPref.getBoolean("Menu", false);

            if(!disableMenu) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, hf).commit();
                        return true;
                    case R.id.navigation_temp:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, tf).commit();
                        return true;
                    case R.id.navigation_engine:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, ef).commit();
                        return true;
                    case R.id.navigation_lights:
                        itemMenuLight = item;
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, lf).commit();
                        return true;
                    case R.id.navigation_phchlorine:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, pf).commit();
                        return true;
                }
            }
            return false;
        }
    };

  /*  @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && itemMenuLight != null){
            calculosAcelerometro(event);
        }
    }

    private void calculosAcelerometro(SensorEvent event){
        float[] values = event.values;
        float x = values[0]; float y = values[1]; float z = values[2];
        long tempoActual = System.currentTimeMillis();
        float aceleracao = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

        if (aceleracao >= 4){
            if (tempoActual - updateTempo < 200 ){ return; }
            updateTempo = tempoActual;
            LightsFragment  lfc = new LightsFragment();
            lfc.sensor_luz(true);
             //lfc.btCentral = (Button) findViewById(R.id.LightCentral);
             //lfc.btCentral.performClick();
        }
    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                PedidoGetSensor();
                PedidoGetHistorico();
                atualizar = true;
                return true;
            case R.id.action_about:
                sobre();
                return true;
            case R.id.action_exit:{
                System.exit(0);
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_logo);
        new CountDownTimer(5000,1000){
            @Override
            public void onTick(long millisUntilFinished){}

            @Override
            public void onFinish(){
                setContentView(R.layout.activity_main);
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, hf).commit();
                BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
                navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
                getSupportActionBar().setElevation(0);
            }
        }.start();

        mDbHelper = new DB(MainActivity.this);
        db = mDbHelper.getReadableDatabase();

        array_equipamento = new ArrayList<>();
        array_valor = new ArrayList<>();

        h_array_equipamento = new ArrayList<>();
        h_array_valor = new ArrayList<>();

        //sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        //updateTempo = System.currentTimeMillis();

        PedidoGetHistorico();
        PedidoGetSensor();
    }

    public void PedidoGetSensor(){

        String url = "http://www.myapps.shared.town/webservices/ws_get_sensores.php";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray(Utils.param_dados);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        array_equipamento.add(obj.getInt("id_equipamento"));
                        array_valor.add(obj.getDouble("valor"));
                    }
                    carregarSensorBD();
                } catch(JSONException ex){}
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Erro de ligação.", Toast.LENGTH_LONG).show();
            }
        });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsObjRequest);

    }

    public void PedidoGetHistorico(){

        String url = "http://www.myapps.shared.town/webservices/ws_get_historico.php";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray(Utils.param_dados);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        h_array_equipamento.add(obj.getInt("id_equipamento"));
                        h_array_valor.add(obj.getDouble("valor"));
                    }
                    carregarHistoricoBD();
                } catch(JSONException ex){}
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Erro de ligação.", Toast.LENGTH_LONG).show();
            }
        });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsObjRequest);

    }

    public void carregarSensorBD(){

        Calendar cal = Calendar.getInstance();
        final int hour_x = cal.get(Calendar.HOUR_OF_DAY);
        final int minute_x = cal.get(Calendar.MINUTE);

        for(int i = 0; i <= array_equipamento.size()-1; i++) {
            ContentValues cv = new ContentValues();
            cv.put(Contrato.Sensor.COLUMN_IDEQUIPAMENTO, array_equipamento.get(i));
            cv.put(Contrato.Sensor.COLUMN_VALOR, array_valor.get(i));
            cv.put(Contrato.Sensor.CALUMN_TIME, hour_x + ":" + minute_x);
            db.insert(Contrato.Sensor.TABLE_NAME, null, cv);
        }
        if(atualizar)
            Toast.makeText(MainActivity.this,"Dados atualizados.", Toast.LENGTH_SHORT).show();
    }

    public void carregarHistoricoBD(){

        for(int i = 0; i <= h_array_equipamento.size()-1; i++) {
            ContentValues cv = new ContentValues();
            cv.put(Contrato.Historico.COLUMN_IDEQUIPAMENTO, h_array_equipamento.get(i));
            cv.put(Contrato.Historico.COLUMN_VALOR, h_array_valor.get(i));
            db.insert(Contrato.Historico.TABLE_NAME, null, cv);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }

    public void sobre(){
        Intent mainAct = new Intent(this, MaterialTutorialActivity.class);
        mainAct.putParcelableArrayListExtra(MaterialTutorialActivity.MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS, getTutorialItems(this));
        startActivityForResult(mainAct, REQUEST_CODE);
    }

    private ArrayList<TutorialItem> getTutorialItems(Context context) {
        TutorialItem tutorialItem1 = new TutorialItem(context.getString(R.string.sensor_title), context.getString(R.string.sensor_subtitle),
                R.color.sensor, R.drawable.poolc1);

        TutorialItem tutorialItem2 = new TutorialItem(context.getString(R.string.engine_title), context.getString(R.string.engine_subtitle),
                R.color.engines, R.drawable.poolc2);

        TutorialItem tutorialItem3 = new TutorialItem(context.getString(R.string.light_title), context.getString(R.string.light_subtitle),
                R.color.light, R.drawable.poolc3);

        TutorialItem tutorialItem4 = new TutorialItem(context.getString(R.string.poolcontrol_title), context.getString(R.string.poolcontrol_subtitle),
                R.color.poolcontrol, R.drawable.poolc4);


        ArrayList<TutorialItem> tutorialItems = new ArrayList<>();
        tutorialItems.add(tutorialItem1);
        tutorialItems.add(tutorialItem2);
        tutorialItems.add(tutorialItem3);
        tutorialItems.add(tutorialItem4);

        return tutorialItems;
    }
}