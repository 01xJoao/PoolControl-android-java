package joaopalma.android.poolcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baoyz.widget.PullRefreshLayout;
import com.baoyz.widget.SmartisanDrawable;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import joaopalma.android.poolcontrol.db.Contrato;
import joaopalma.android.poolcontrol.db.DB;

public class HomeFragment extends Fragment {
    // private OnFragmentInteractionListener mListener;
    PullRefreshLayout layout;

    int cobertura = 1;
    int luzCentral = 8;
    int sensor_temp = 7;
    int sensor_ph = 13;
    int sensor_cloro = 14;
    int agendaMotor = 17;
    int agendaRobo = 18;

    int valorLuzCentral;
    int valorCobertura;
    float valorSensorTemp;
    float valorSensorPh;
    float valorSensorCloro;

    SwitchCompat switchLight;
    SwitchCompat switchCobertura;

    DB mDbHelper;
    SQLiteDatabase db;
    Cursor c_historico, c_sensor, c_agendamento;

    int hour_x;
    int minute_x;
    String time;

    int array_equipamento[];
    int array_valor[];

    int array_sensor[];
    float array_sensor_valor[];

    ArrayList<Integer> array_motor_agenda;
    ArrayList<Integer> array_robot_agenda;
    ArrayList<String> array_motor_agenda_time;
    ArrayList<String> array_robot_agenda_time;

    boolean HoraEncontradaMotor;
    boolean HoraEncontradaRobot;

    String finaltimeMotor;
    String finaltimeRobot;

    TextView ButtonTempValue;
    TextView ButtonphValue;
    TextView ButtonCloroValue;

    TextView MotorHour;
    TextView RobotHour;

    SharedPreferences.Editor editor;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.app_name));

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putBoolean("Menu", true);
        editor.commit();

        array_motor_agenda = new ArrayList<>();
        array_robot_agenda = new ArrayList<>();
        array_motor_agenda_time = new ArrayList<>();
        array_robot_agenda_time = new ArrayList<>();

        ButtonTempValue = (TextView) getActivity().findViewById(R.id.textView_valueTemp);
        ButtonphValue = (TextView) getActivity().findViewById(R.id.textView_valuePh);
        ButtonCloroValue = (TextView) getActivity().findViewById(R.id.textView_valueCloro);
        MotorHour = (TextView) getActivity().findViewById(R.id.textView_valueEngine);
        RobotHour = (TextView) getActivity().findViewById(R.id.textView_valueRobot);

        switchCobertura = (SwitchCompat) getActivity().findViewById(R.id.switch_cobertura);
        switchLight = (SwitchCompat) getActivity().findViewById(R.id.switch_light);

        layout = (PullRefreshLayout) getActivity().findViewById(R.id.swipeRefreshLayout);
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                layout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        preencherDados();
                        layout.setRefreshing(false);
                    }
                }, 3000);
            }
        });
        layout.setColorSchemeColors(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
        layout.setRefreshDrawable(new SmartisanDrawable(getActivity(), layout));


        switchCobertura.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switchCobertura.isPressed()) {
                    int myInt = (isChecked) ? 1 : 0;
                    EnviarPedido(cobertura, myInt);
                }
            }
        });


        switchLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switchLight.isPressed()) {
                    int myInt = (isChecked) ? 1 : 0;
                    EnviarPedido(luzCentral, myInt);
                }
            }
        });

        /* Hora */

        Calendar cal = Calendar.getInstance();
        hour_x = cal.get(Calendar.HOUR_OF_DAY);
        minute_x = cal.get(Calendar.MINUTE);

        if (hour_x >= 10 && minute_x >= 10) {
            time = hour_x + ":" + minute_x + ":00";
        } else if (hour_x >= 10 && minute_x < 10) {
            time = hour_x + ":0" + minute_x + ":00";
        } else if (hour_x < 10 && minute_x >= 10) {
            time = "0" + hour_x + ":" + minute_x + ":00";
        } else {
            time = "0" + hour_x + ":0" + minute_x + ":00";
        }

        mDbHelper = new DB(getActivity());
        db = mDbHelper.getReadableDatabase();

        preencherDados();
    }

    public void preencherDados(){
        c_historico = db.query(false, Contrato.Historico.TABLE_NAME, Contrato.Historico.PROJECTION,
                "id_equipamento = ? OR id_equipamento = ?", new String[]{String.valueOf(cobertura),String.valueOf(luzCentral)}, null, null, null, null);


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

        c_sensor = db.query(false, Contrato.Sensor.TABLE_NAME, Contrato.Sensor.PROJECTION, null , null, null, null, null, null);

        array_sensor = new int[c_sensor.getCount()];
        array_sensor_valor = new float[c_sensor.getCount()];

        int j = 0;
        if(c_sensor.getCount() > 0) {
            c_sensor.moveToFirst();
            while (!c_sensor.isAfterLast()){
                array_sensor[j] = c_sensor.getInt(1);
                array_sensor_valor[j] = c_sensor.getFloat(2);
                c_sensor.moveToNext();
                j++;
            }
        }

        c_agendamento = db.query(false, Contrato.Agendamento.TABLE_NAME, Contrato.Agendamento.PROJECTION, null, null, null, null, null, null);

        if(c_agendamento.getCount() > 0){
            c_agendamento.moveToFirst();
            while (!c_agendamento.isAfterLast()){

                if(c_agendamento.getInt(1) == agendaMotor){
                    array_motor_agenda.add(c_agendamento.getInt(1));
                    array_motor_agenda_time.add(c_agendamento.getString(2));
                }
                else{
                    array_robot_agenda.add(c_agendamento.getInt(1));
                    array_robot_agenda_time.add(c_agendamento.getString(2));
                }
                c_agendamento.moveToNext();
            }
        }
        EscolherDados();
    }

    public void  EscolherDados(){

        for(int i=0; i<=(array_equipamento.length-1);i++){
            if(array_equipamento[i] == cobertura){
                valorCobertura = array_valor[i];
            }
            else if(array_equipamento[i] == luzCentral){
                valorLuzCentral = array_valor[i];
            }
        }

        for(int i=0; i<=(array_sensor.length-1); i++){
            if(array_sensor[i] == sensor_temp){
                valorSensorTemp = array_sensor_valor[i];
            }
            else if(array_sensor[i] == sensor_ph){
                valorSensorPh = array_sensor_valor[i];
            }
            else if(array_sensor[i] == sensor_cloro){
                valorSensorCloro = array_sensor_valor[i];
            }
        }

        Collections.sort(array_motor_agenda_time);
        Collections.sort(array_robot_agenda_time);


        for(int i=0; i <= array_motor_agenda.size()-1; i++){
            int timedb = Integer.parseInt(array_motor_agenda_time.get(i).replaceAll("[\\D]",""));
            int timenow = Integer.parseInt(time.replaceAll("[\\D]",""));

            if(timenow <= timedb){
                finaltimeMotor = array_motor_agenda_time.get(i);
                HoraEncontradaMotor = true;
                break;
            }
            else{
                HoraEncontradaMotor = false;
            }
        }

        for(int i=0; i <= array_robot_agenda.size()-1; i++){
            int timedb = Integer.parseInt(array_robot_agenda_time.get(i).replaceAll("[\\D]",""));
            int timenow = Integer.parseInt(time.replaceAll("[\\D]",""));

            if(timenow <= timedb){
                finaltimeRobot = array_robot_agenda_time.get(i);
                HoraEncontradaRobot = true;
                break;
            }
            else{
                HoraEncontradaRobot = false;
            }
        }

        if(!HoraEncontradaMotor)
            finaltimeMotor = array_motor_agenda_time.get(0);

        if(!HoraEncontradaRobot)
            finaltimeRobot = array_robot_agenda_time.get(0);

        MostrarDados();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mostrarSwitch();
            }
        }, 500);
    }

    public void MostrarDados(){

        ButtonTempValue.setText(""+Math.round(valorSensorTemp)+"º");
        ButtonphValue.setText("" + valorSensorPh);
        ButtonCloroValue.setText("" + valorSensorCloro);

        MotorHour.setText(finaltimeMotor.substring(0, finaltimeMotor.length() - 3));
        RobotHour.setText(finaltimeRobot.substring(0, finaltimeRobot.length() - 3));

        editor.putBoolean("Menu", false);
        editor.commit();
    }

    public void mostrarSwitch(){
        if(valorLuzCentral == 1)
            switchLight.setChecked(true);
        else
            switchLight.setChecked(false);

        if(valorCobertura == 1)
            switchCobertura.setChecked(true);
        else
            switchCobertura.setChecked(false);
    }

    public void EnviarPedido(final int equipamento, final int valor){

        editor.putBoolean("Menu", true);
        editor.commit();

        String url = "http://www.myapps.shared.town/webservices/ws_insert_historico.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonoutput = new JSONObject(response);
                } catch (JSONException ex) {
                    ContentValues cv = new ContentValues();
                    cv.put(Contrato.Historico.COLUMN_VALOR, valor);
                    db.update(Contrato.Historico.TABLE_NAME, cv, Contrato.Historico.COLUMN_IDEQUIPAMENTO + " = ?", new String[]{String.valueOf(equipamento)});
                    editor.putBoolean("Menu", false);
                    editor.commit();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),"Erro de ligação.", Toast.LENGTH_LONG).show();
                if(equipamento == cobertura){
                    if(valor == 0)
                        switchCobertura.setChecked(true);
                    else
                        switchCobertura.setChecked(false);
                }
                if(equipamento == luzCentral){
                    if(valor == 0)
                        switchLight.setChecked(true);
                    else
                        switchLight.setChecked(false);
                }
                editor.putBoolean("Menu", false);
                editor.commit();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id_equipamento", String.valueOf(equipamento));
                params.put("valor", String.valueOf(valor));
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

        if(!c_sensor.isClosed()){
            c_sensor.close();
            c_sensor = null;
        }
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
/*    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
