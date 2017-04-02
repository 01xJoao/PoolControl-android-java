package joaopalma.android.poolcontrol;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.baoyz.widget.PullRefreshLayout;
import com.baoyz.widget.SmartisanDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import joaopalma.android.poolcontrol.db.Contrato;
import joaopalma.android.poolcontrol.db.DB;

public class EnginesFragment extends DialogFragment {

    ArrayList<String> enginesHrs;
    ArrayList<String> enginesDuration;
    ArrayList<String> robotHrs;
    ArrayList<String> robotDuration;
    PullRefreshLayout layout;

    boolean process_switch;

    int motor = 15;
    int robo = 16;
    int motor_automatico = 17;
    int robo_automatico = 18;

    int duracaomotor = 0;
    int duracaorobo = 0;
    int duracaomotor_automatico = 0;
    int duracaorobo_automatico = 0;

    SwitchCompat switchEngine;
    SwitchCompat switchRobot;

    DB mDbHelper;
    SQLiteDatabase db;

    /* Horas Picker*/

    TextView HrsTVAuto;
    TextView HrsTVAutoRobot;

    String hrsDevice;

    static final int DIALONG_ID = 0;
    int hour_x;
    int minute_x;

    boolean actualizado = false;

    public void showTimePickerDialog () {
        HrsTVAuto = (TextView) getActivity().findViewById(R.id.TV_Timepicker);
        HrsTVAutoRobot = (TextView) getActivity().findViewById(R.id.TV_Timepicker_Robot);

        Calendar cal = Calendar.getInstance();

        hour_x = cal.get(Calendar.HOUR_OF_DAY);
        minute_x = cal.get(Calendar.MINUTE);

        if(minute_x >= 10) {
            HrsTVAuto.setText(hour_x + ":" + minute_x);
            HrsTVAutoRobot.setText(hour_x + ":" + minute_x);
        }
        else{
            HrsTVAuto.setText(hour_x + ":0" + minute_x);
            HrsTVAutoRobot.setText(hour_x + ":0" + minute_x);
        }

        HrsTVAuto.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    hrsDevice = "motor";
                    onCreateDialog(0).show();
                }
            }
        );

        HrsTVAutoRobot.setOnClickListener(new View.OnClickListener(){
                 @Override
                 public void onClick(View v) {
                     hrsDevice = "robo";
                     onCreateDialog(0).show();
                 }
             }
        );
    }

    protected Dialog onCreateDialog(int id) {

        if(id == DIALONG_ID)
            return new TimePickerDialog(getActivity(), R.style.TimePickerTheme ,kTimePickerListener, hour_x, minute_x, true);
        return null;
    }

    protected TimePickerDialog.OnTimeSetListener kTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour_x = hourOfDay;
            minute_x = minute;
            if (hrsDevice == "motor") {
                if(minute_x >= 10) {
                    HrsTVAuto.setText(hour_x + ":" + minute_x);
                }
                else{
                    HrsTVAuto.setText(hour_x + ":0" + minute_x);
                }
            }
            else {
                if(minute_x >= 10) {
                    HrsTVAutoRobot.setText(hour_x + ":" + minute_x);
                }
                else{
                    HrsTVAutoRobot.setText(hour_x + ":0" + minute_x);
                }
            }
        }
    };


    public EnginesFragment() {
    }

    public static EnginesFragment newInstance() {
        EnginesFragment fragment = new EnginesFragment();
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
        process_switch = true;

        if(isAdded()) {

            getActivity().setTitle("Engenhos");

            ReceberAgenda();

            enginesHrs = new ArrayList<>();
            enginesDuration = new ArrayList<>();
            robotHrs = new ArrayList<>();
            robotDuration = new ArrayList<>();

            layout = (PullRefreshLayout) getActivity().findViewById(R.id.swipeRefreshLayout);
            layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    layout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            layout.setRefreshing(false);
                        }
                    }, 3000);
                }
            });
            layout.setColorSchemeColors(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
            layout.setRefreshDrawable(new SmartisanDrawable(getActivity(), layout));

            /* Horas Picker*/

            showTimePickerDialog();

            /* Spinners */

            final Spinner EngineManualSpinneer = (Spinner) getActivity().findViewById(R.id.hrs_spinner);
            ArrayAdapter<CharSequence> Adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.work_hrs, android.R.layout.simple_spinner_item);
            Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            EngineManualSpinneer.setAdapter(Adapter);

            final Spinner EngineAutomaticSpinneer = (Spinner) getActivity().findViewById(R.id.automatic_duration_spinner);
            ArrayAdapter<CharSequence> AdapterAutomatic = ArrayAdapter.createFromResource(this.getActivity(), R.array.work_hrs, android.R.layout.simple_spinner_item);
            AdapterAutomatic.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            EngineAutomaticSpinneer.setAdapter(AdapterAutomatic);

            final Spinner RobotManualSpinneer = (Spinner) getActivity().findViewById(R.id.hrs_spinner_robot);
            ArrayAdapter<CharSequence> AdapterRobot = ArrayAdapter.createFromResource(this.getActivity(), R.array.work_hrs, android.R.layout.simple_spinner_item);
            AdapterRobot.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            RobotManualSpinneer.setAdapter(AdapterRobot);

            final Spinner RobotAutomaticSpinneer = (Spinner) getActivity().findViewById(R.id.automatic_duration_spinner_robot);
            ArrayAdapter<CharSequence> AdapterAutomaticRobot = ArrayAdapter.createFromResource(this.getActivity(), R.array.work_hrs, android.R.layout.simple_spinner_item);
            AdapterAutomaticRobot.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            RobotAutomaticSpinneer.setAdapter(AdapterAutomaticRobot);

            /* TABS */

            final TextView controlView = (TextView) getActivity().findViewById(R.id.tview_engines_engine);
            final LinearLayout controlLayout = (LinearLayout) getActivity().findViewById(R.id.card_control_layout_engine);

            final TextView historyView = (TextView) getActivity().findViewById(R.id.tview_engines_robot);
            final LinearLayout historyLayout = (LinearLayout) getActivity().findViewById(R.id.card_history_layout_engine);

            controlLayout.setVisibility(View.VISIBLE);
            historyLayout.setVisibility(View.GONE);
            controlView.setPaintFlags(controlView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            historyView.setPaintFlags(controlView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

            controlView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlLayout.setVisibility(View.VISIBLE);
                    historyLayout.setVisibility(View.GONE);
                    controlView.setPaintFlags(controlView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    historyView.setPaintFlags(controlView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                }
            });

            historyView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlLayout.setVisibility(View.GONE);
                    historyLayout.setVisibility(View.VISIBLE);
                    controlView.setPaintFlags(controlView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                    historyView.setPaintFlags(controlView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            });

            /* POST & BOTOES */

            Button btnMotorManual = (Button) getActivity().findViewById(R.id.start_engineManual);
            Button btnRoboManual = (Button) getActivity().findViewById(R.id.start_robotManual);

            btnMotorManual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "A iniciar motor", Toast.LENGTH_SHORT).show();
                    duracaomotor = EngineManualSpinneer.getSelectedItemPosition();
                    EnviarPedido(motor, duracaomotor + 1);
                }

            });

            btnRoboManual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "A iniciar robô", Toast.LENGTH_SHORT).show();
                    duracaorobo = RobotManualSpinneer.getSelectedItemPosition();
                    EnviarPedido(robo, duracaorobo + 1);
                }

            });

            /* SWITCH ENGINE & ROBOT */

            switchEngine = (SwitchCompat) getActivity().findViewById(R.id.switch_engine);
            switchEngine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (process_switch) {
                        int myInt = (isChecked) ? 1 : 0;
                        EnviarPedido(motor_automatico, myInt);
                    }
                }
            });

            switchRobot = (SwitchCompat) getActivity().findViewById(R.id.switch_robot);
            switchRobot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int myInt = (isChecked) ? 1 : 0;
                    if (process_switch) {
                        EnviarPedido(robo_automatico, myInt);
                    }
                }
            });

            /* AGENDAMENTO */

            final Button agendamentoMotor = (Button) getActivity().findViewById(R.id.add_engine);
            final Button agendamentoRobo = (Button) getActivity().findViewById(R.id.add_robot);

            agendamentoMotor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String time;
                    if (hour_x >= 10 && minute_x >= 10) {
                        time = String.valueOf(hour_x) + ":" + String.valueOf(minute_x) + ":00";
                    } else if (hour_x >= 10 && minute_x < 10) {
                        time = String.valueOf(hour_x) + ":0" + String.valueOf(minute_x) + ":00";
                    } else if (hour_x < 10 && minute_x >= 10) {
                        time = "0" + String.valueOf(hour_x) + ":" + String.valueOf(minute_x) + ":00";
                    } else {
                        time = "0" + String.valueOf(hour_x) + ":0" + String.valueOf(minute_x) + ":00";
                    }
                    Toast.makeText(getActivity(), "A agendar..", Toast.LENGTH_SHORT).show();
                    duracaomotor_automatico = EngineAutomaticSpinneer.getSelectedItemPosition();
                    EnviarAgenda(motor_automatico, time, duracaomotor_automatico + 1);

                }
            });

            agendamentoRobo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String time;
                    if (hour_x >= 10 && minute_x >= 10) {
                        time = String.valueOf(hour_x) + ":" + String.valueOf(minute_x) + ":00";
                    } else if (hour_x >= 10 && minute_x < 10) {
                        time = String.valueOf(hour_x) + ":0" + String.valueOf(minute_x) + ":00";
                    } else if (hour_x < 10 && minute_x >= 10) {
                        time = "0" + String.valueOf(hour_x) + ":" + String.valueOf(minute_x) + ":00";
                    } else {
                        time = "0" + String.valueOf(hour_x) + ":0" + String.valueOf(minute_x) + ":00";
                    }
                    Toast.makeText(getActivity(), "A agendar..", Toast.LENGTH_SHORT).show();
                    duracaorobo_automatico = RobotAutomaticSpinneer.getSelectedItemPosition();
                    EnviarAgenda(robo_automatico, time, duracaorobo_automatico + 1);
                }
            });
        }
    }

    public void EnviarPedido(final int equipamento, final int valor){

        String url = "https://poolcontrol.000webhostapp.com/webservices/ws_insert_historico.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonoutput = new JSONObject(response);
                } catch (JSONException ex) {
                    ContentValues cv = new ContentValues();
                    cv.put(Contrato.Historico.COLUMN_VALOR, valor);
                    db.update(Contrato.Historico.TABLE_NAME, cv, Contrato.Historico.COLUMN_IDEQUIPAMENTO + " = ?", new String[]{String.valueOf(equipamento)});
                    if(equipamento == motor || equipamento == robo)
                        Toast.makeText(getActivity(), "Limpeza iniciada", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),"Erro:  " + String.valueOf(error), Toast.LENGTH_LONG).show();
                process_switch = false;
                if(equipamento == motor_automatico){
                    if(valor == 0)
                        switchEngine.setChecked(true);
                    else
                        switchEngine.setChecked(false);
                }
                if(equipamento == robo_automatico){
                    if(valor == 0)
                        switchRobot.setChecked(true);
                    else
                        switchRobot.setChecked(false);
                }
                process_switch = true;
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

    public void EnviarAgenda(final int equipamento, final String time, final int duracao){

        String url = "https://poolcontrol.000webhostapp.com/webservices/ws_insert_agendamento.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonoutput = new JSONObject(response);
                } catch (JSONException ex) {
                    //ReceberAgenda();
                    ContentValues cv = new ContentValues();
                    cv.put(Contrato.Agendamento.COLUMN_IDEQUIPAMENTO, equipamento);
                    cv.put(Contrato.Agendamento.COLUMN_INICIO, time);
                    cv.put(Contrato.Agendamento.COLUMN_TEMPO, duracao);
                    db.insert(Contrato.Agendamento.TABLE_NAME, null, cv);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),"Erro: " + String.valueOf(error), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id_equipamento", String.valueOf(equipamento));
                params.put("inicio", time);
                params.put("tempo_duracao", String.valueOf(duracao));
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

    public void ReceberAgenda(){

        String url_get = "https://poolcontrol.000webhostapp.com/webservices/ws_get_agendamento.php";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url_get, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray(Utils.param_dados);

                    enginesHrs.clear();
                    enginesDuration.clear();
                    robotHrs.clear();
                    robotDuration.clear();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        if(obj.getString("id_equipamento").equals("17")){
                            enginesHrs.add(obj.getString("inicio"));
                            enginesDuration.add(obj.getString("tempo_duracao"));
                        }
                        else{
                            robotHrs.add(obj.getString("inicio"));
                            robotDuration.add(obj.getString("tempo_duracao"));
                        }
                    }
                    MostrarAgenda();
                } catch(JSONException ex){}
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),"Erro GET: "+String.valueOf(error), Toast.LENGTH_LONG).show();
            }
        });

        MySingleton.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
    }

    public void MostrarAgenda(){

        final String [] ArrayenginesHrs = enginesHrs.toArray(new String[enginesHrs.size()]);
        final String [] ArrayenginesDuration = enginesDuration.toArray(new String[enginesDuration.size()]);

        final String [] ArrayrobotHrs = robotHrs.toArray(new String[robotHrs.size()]);
        final String [] ArrayrobotDuration = robotDuration.toArray(new String[robotDuration.size()]);

        /* ENGINE AND ROBOT LIST VIEWS*/

        ListView lvEngine = (ListView) getActivity().findViewById(R.id.engines_listview_engine);
        Adapter adapterEngine = new Adapter(getActivity(), ArrayenginesHrs, ArrayenginesDuration);
        lvEngine.setAdapter(adapterEngine);

        lvEngine.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(),ArrayenginesHrs[position], Toast.LENGTH_SHORT).show();
            }
        });

        ListView lvRobot = (ListView) getActivity().findViewById(R.id.engines_listview_robot);
        Adapter adapterRobot = new Adapter(getActivity(), ArrayrobotHrs, ArrayrobotDuration);
        lvRobot.setAdapter(adapterRobot);

        lvRobot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(),ArrayrobotHrs[position], Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_engines, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
   /*  public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
