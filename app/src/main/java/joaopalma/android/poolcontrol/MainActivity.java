package joaopalma.android.poolcontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.util.HashMap;
import java.util.Map;

import joaopalma.android.poolcontrol.db.Contrato;
import joaopalma.android.poolcontrol.db.DB;


public class MainActivity extends AppCompatActivity {

    HomeFragment hf = HomeFragment.newInstance();
    TempFragment tf = TempFragment.newInstance();
    EnginesFragment ef = EnginesFragment.newInstance();
    LightsFragment lf = LightsFragment.newInstance();
    phcloroFragment pf = phcloroFragment.newInstance();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, lf).commit();
                    return true;
                case R.id.navigation_phchlorine:
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, pf).commit();
                    return true;
            }
            return false;
        }

    };

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
                return true;
            case R.id.action_notifications:
                //notifications();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, hf).commit();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        getSupportActionBar().setElevation(0);

        // PedidoGET();
    }

    /*public void PedidoGET(){

       String url_get = "https://poolcontrol.000webhostapp.com/webservices/ws_get_historico.php";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url_get, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray(Utils.param_dados);
                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    editor.clear();

                    editor.putInt("equipamentos" + "_size", array.length());

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        editor.putString("id_equipamento"+ "_" + i, "d_" + obj.getString("id_equipamento"));
                        editor.putInt("valor"+"_" + i, obj.getInt("valor"));
                        editor.commit();
                    }
                } catch(JSONException ex){}
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Erro GET: "+String.valueOf(error), Toast.LENGTH_LONG).show();
            }
        });

        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsObjRequest);

    }*/
}
