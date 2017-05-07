package joaopalma.android.poolcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import joaopalma.android.poolcontrol.db.Contrato;
import joaopalma.android.poolcontrol.db.DB;

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.Sensor.TYPE_ACCELEROMETER;

public class LightsFragment extends Fragment {
    private SensorManager sensorManager;
    private Sensor mAccelerometerSensor;
    private long updateTempo;
    boolean lightCentralBool = true;
    boolean lightSmall1Bool = true;
    boolean lightSmall2Bool = true;
    boolean lightSmall3Bool = true;
    boolean lightSmall4Bool = true;

    int LuzCentral = 8;
    int LuzFrenteEsq = 9;
    int LuzFrenteDir = 10;
    int LuzTrasEsq = 11;
    int LuzTrasDir = 12;

    Button btCentral;
    Button btSmall1;
    Button btSmall2;
    Button btSmall3;
    Button btSmall4;

    DB mDbHelper;
    SQLiteDatabase db;
    Cursor c_historico;

    int array_equipamento[];
    int array_valor[];

    boolean Sensor = false;

    SharedPreferences.Editor editor;

    public LightsFragment() {}

    public static LightsFragment newInstance() {
        LightsFragment fragment = new LightsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDbHelper = new DB(getActivity());
        db = mDbHelper.getReadableDatabase();

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        mAccelerometerSensor = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        updateTempo = System.currentTimeMillis();

        if (isAdded()) {
            getActivity().setTitle(getResources().getString(R.string.title_lights));

            btCentral = (Button) getActivity().findViewById(R.id.LightCentral);
            btSmall1 = (Button) getActivity().findViewById(R.id.LightSmall1);
            btSmall2 = (Button) getActivity().findViewById(R.id.LightSmall2);
            btSmall3 = (Button) getActivity().findViewById(R.id.LightSmall3);
            btSmall4 = (Button) getActivity().findViewById(R.id.LightSmall4);

            btCentral.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Sensor){
                        Toast.makeText(getActivity(), ""+Sensor, Toast.LENGTH_SHORT).show();
                        Luzes();
                        Sensor = false;
                    }
                    else {
                        EnviarPedidos(LuzCentral, lightCentralBool);
                        if (lightCentralBool) {
                            btCentral.setBackgroundResource(R.drawable.button_round_corners_light_on);
                        } else {
                            btCentral.setBackgroundResource(R.drawable.button_round_corners_light_off);
                        }
                    }
                    lightCentralBool = !lightCentralBool;
                }
            });

            btSmall1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnviarPedidos(LuzFrenteEsq, lightSmall1Bool);
                    if (lightSmall1Bool) {
                        btSmall1.setBackgroundResource(R.drawable.button_round_corners_light_on);
                    } else {
                        btSmall1.setBackgroundResource(R.drawable.button_round_corners_light_off);
                    }
                    lightSmall1Bool = !lightSmall1Bool;
                }
            });

            btSmall2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnviarPedidos(LuzFrenteDir, lightSmall2Bool);
                    if (lightSmall2Bool) {
                        btSmall2.setBackgroundResource(R.drawable.button_round_corners_light_on);
                    } else {
                        btSmall2.setBackgroundResource(R.drawable.button_round_corners_light_off);
                    }
                    lightSmall2Bool = !lightSmall2Bool;
                }
            });

            btSmall3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnviarPedidos(LuzTrasEsq, lightSmall3Bool);
                    if (lightSmall3Bool) {
                        btSmall3.setBackgroundResource(R.drawable.button_round_corners_light_on);
                    } else {
                        btSmall3.setBackgroundResource(R.drawable.button_round_corners_light_off);
                    }
                    lightSmall3Bool = !lightSmall3Bool;
                }
            });

            btSmall4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnviarPedidos(LuzTrasDir, lightSmall4Bool);
                    if (lightSmall4Bool) {
                        btSmall4.setBackgroundResource(R.drawable.button_round_corners_light_on);
                    } else {
                        btSmall4.setBackgroundResource(R.drawable.button_round_corners_light_off);
                    }
                    lightSmall4Bool = !lightSmall4Bool;
                }
            });
        }
        preencherDados();
    }

    public void preencherDados(){
        c_historico = db.query(false, Contrato.Historico.TABLE_NAME, Contrato.Historico.PROJECTION,
                "id_equipamento = ? OR id_equipamento = ? OR id_equipamento = ? OR id_equipamento = ? OR id_equipamento = ?",
                    new String[]{String.valueOf(LuzCentral), String.valueOf(LuzFrenteEsq), String.valueOf(LuzFrenteDir),
                            String.valueOf(LuzTrasEsq), String.valueOf(LuzTrasDir)}, null, null, null, null);

        c_historico.moveToFirst();

        array_equipamento = new int[c_historico.getCount()];
        array_valor = new int[c_historico.getCount()];

        int i = 0;
        if(c_historico.getCount() > 0) {
            c_historico.moveToFirst();
            while (!c_historico.isAfterLast()){
                array_equipamento[i] = c_historico.getInt(1);
                array_valor[i] = c_historico.getInt(2);
                c_historico.moveToNext();
                i++;
            }
        }

        escolherDados();
    }

    public void  escolherDados(){

        for(int i=0; i<=(array_equipamento.length-1);i++){
            if(array_equipamento[i] == LuzCentral){
                lightCentralBool = (array_valor[i] != 0);
            }
            else if(array_equipamento[i] == LuzFrenteEsq){
                lightSmall1Bool = (array_valor[i] != 0);
            }
            else if(array_equipamento[i] == LuzFrenteDir){
                lightSmall2Bool = (array_valor[i] != 0);
            }
            else if(array_equipamento[i] == LuzTrasEsq){
                lightSmall3Bool = (array_valor[i] != 0);
            }
            else if(array_equipamento[i] == LuzTrasDir){
                lightSmall4Bool = (array_valor[i] != 0);
            }
        }
        mostrarDados();
    }

    public void mostrarDados(){

        if (lightCentralBool) {
            btCentral.setBackgroundResource(R.drawable.button_round_corners_light_on);
            lightCentralBool = !lightCentralBool;
        } else {
            btCentral.setBackgroundResource(R.drawable.button_round_corners_light_off);
            lightCentralBool = !lightCentralBool;
        }

        if (lightSmall1Bool) {
            btSmall1.setBackgroundResource(R.drawable.button_round_corners_light_on);
            lightSmall1Bool = !lightSmall1Bool;
        } else {
            btSmall1.setBackgroundResource(R.drawable.button_round_corners_light_off);
            lightSmall1Bool = !lightSmall1Bool;
        }

        if (lightSmall2Bool) {
            btSmall2.setBackgroundResource(R.drawable.button_round_corners_light_on);
            lightSmall2Bool = !lightSmall2Bool;
        } else {
            btSmall2.setBackgroundResource(R.drawable.button_round_corners_light_off);
            lightSmall2Bool = !lightSmall2Bool;
        }

        if (lightSmall3Bool) {
            btSmall3.setBackgroundResource(R.drawable.button_round_corners_light_on);
            lightSmall3Bool = !lightSmall3Bool;
        } else {
            btSmall3.setBackgroundResource(R.drawable.button_round_corners_light_off);
            lightSmall3Bool = !lightSmall3Bool;
        }

        if (lightSmall4Bool) {
            btSmall4.setBackgroundResource(R.drawable.button_round_corners_light_on);
            lightSmall4Bool = !lightSmall4Bool;
        } else {
            btSmall4.setBackgroundResource(R.drawable.button_round_corners_light_off);
            lightSmall4Bool = !lightSmall4Bool;
        }
    }

    final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            switch (sensorEvent.sensor.getType()) {
                case TYPE_ACCELEROMETER:
                    calculosAcelerometro(sensorEvent);
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    };

    private void calculosAcelerometro(SensorEvent event){
        float[] values = event.values;
        float x = values[0]; float y = values[1]; float z = values[2];
        long tempoActual = System.currentTimeMillis();
        float aceleracao = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

        if (aceleracao >= 5){
            if (tempoActual - updateTempo < 200 ){ return; }
            updateTempo = tempoActual;
            Luzes();

        }
    }

   public void Luzes(){
       if (lightCentralBool == true)
       {
           lightCentralBool = lightSmall1Bool = lightSmall2Bool = lightSmall3Bool = lightSmall4Bool = true;
           EnviarPedidos(LuzCentral, true);
           EnviarPedidos(LuzFrenteEsq, true);
           EnviarPedidos(LuzFrenteDir, true);
           EnviarPedidos(LuzTrasEsq, true);
           EnviarPedidos(LuzTrasDir, true);
       }
       else {
           lightCentralBool = lightSmall1Bool = lightSmall2Bool = lightSmall3Bool = lightSmall4Bool = false;
           EnviarPedidos(LuzCentral, false);
           EnviarPedidos(LuzFrenteEsq, false);
           EnviarPedidos(LuzFrenteDir, false);
           EnviarPedidos(LuzTrasEsq, false);
           EnviarPedidos(LuzTrasDir, false);
       }
       mostrarDados();
   }

    public void EnviarPedidos(final int equipamento, final boolean light){
        editor.putBoolean("Menu", true);
        editor.commit();

        final int mylight = (light) ? 1 : 0;

        String url = "http://www.myapps.shared.town/webservices/ws_insert_historico.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonoutput = new JSONObject(response);
                    //Toast.makeText(getActivity(), jsonoutput.getString(Utils.param_status), Toast.LENGTH_SHORT).show();
                } catch (JSONException ex) {
                    if (isAdded()) {
                        ContentValues cv = new ContentValues();
                        cv.put(Contrato.Historico.COLUMN_VALOR, mylight);
                        db.update(Contrato.Historico.TABLE_NAME, cv, Contrato.Historico.COLUMN_IDEQUIPAMENTO + " = ?", new String[]{String.valueOf(equipamento)});
                        editor.putBoolean("Menu", false);
                        editor.commit();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),"Erro de ligação.", Toast.LENGTH_LONG).show();
                editor.putBoolean("Menu", false);
                editor.commit();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id_equipamento", String.valueOf(equipamento));
                params.put("valor", String.valueOf(mylight));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lights, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
        if(!c_historico.isClosed()){
            c_historico.close();
            c_historico = null;
        }

        if(db.isOpen()){
            db.close();
            db = null;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /* public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    } */
}
