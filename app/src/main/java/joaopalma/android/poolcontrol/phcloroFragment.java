package joaopalma.android.poolcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.baoyz.widget.PullRefreshLayout;
import com.baoyz.widget.SmartisanDrawable;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sccomponents.widgets.ScArcGauge;
import com.sccomponents.widgets.ScGauge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import joaopalma.android.poolcontrol.db.Contrato;
import joaopalma.android.poolcontrol.db.DB;

public class phcloroFragment extends Fragment {
    // private OnFragmentInteractionListener mListener;
    PullRefreshLayout layout;

    int device_ph = 13;
    int device_cloro = 14;

    float valorpH;
    float valorCloro;

    String get_ph;
    String get_cloro;

    DB mDbHelper;
    SQLiteDatabase db;
    Cursor c_sensor, c_historico;

    TextView phValue;
    TextView cloroValue;

    int[] array_equipamento;
    float[] array_valor;

    int[] array_equipamento_historico;
    int[] array_valor_historico;

    ImageView indicator;
    ImageView indicator_cloro;

    Button NormalizarPH;
    Button NormalizarCloro;

    double MAX_PH = 8;
    double MAX_CLORO = 6;

    int valorpHDesejado;
    int valorCloroDesejado;

    SharedPreferences.Editor editor;

    public phcloroFragment() {
    }

    public static phcloroFragment newInstance() {
        phcloroFragment fragment = new phcloroFragment();
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

        if (isAdded()) {

            getActivity().setTitle(getResources().getString(R.string.title_phchlorine));

            phValue = (TextView) getActivity().findViewById(R.id.ph_value);
            cloroValue = (TextView) getActivity().findViewById(R.id.cloro_value);
            NormalizarPH = (Button) getActivity().findViewById(R.id.normalizar_ph);
            NormalizarCloro = (Button) getActivity().findViewById(R.id.normalizar_cloro);

            /* Receber valores*/
            preencherDados();

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

            final TextView controlView = (TextView) getActivity().findViewById(R.id.tview_phcloro_control);
            final LinearLayout controlLayout = (LinearLayout) getActivity().findViewById(R.id.control_phcloro);
            final TextView historyView = (TextView) getActivity().findViewById(R.id.tview_phcloro_history);
            final LinearLayout historyLayout = (LinearLayout) getActivity().findViewById(R.id.history_phcloro);

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


            /* Enviar POST*/

            NormalizarPH.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(valorpHDesejado != 0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
                        builder.setTitle("A normalizar...");
                        builder.setMessage("Deseja cancelar a normalização do pH?")
                                .setCancelable(false)
                                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        valorpHDesejado = 0;
                                        EnviarPedidosPOST(device_ph, "ph");
                                    }
                                })
                                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else {
                        if (valorpH < MAX_PH / 2) {
                            valorpHDesejado = 1;
                            EnviarPedidosGET(device_ph, "ph");
                            Toast.makeText(getActivity(), "Pedido de normalização pH", Toast.LENGTH_SHORT).show();
                        } else {
                            NormalizarPH.setText(getResources().getString(R.string.button_normalize));
                            NormalizarPH.setBackgroundResource(R.drawable.button_round_corners_normalize);
                            Toast.makeText(getActivity(), "Valor de pH demasiado alto para normalizar", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            NormalizarCloro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(valorCloroDesejado != 0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
                        builder.setTitle("A normalizar...");
                        builder.setMessage("Deseja cancelar a normalização do cloro?")
                                .setCancelable(false)
                                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        valorCloroDesejado = 0;
                                        EnviarPedidosPOST(device_cloro, "cloro");
                                    }
                                })
                                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else {
                        if (valorCloro < MAX_CLORO / 2) {
                            valorCloroDesejado = 1;
                            EnviarPedidosGET(device_cloro, "cloro");
                            Toast.makeText(getActivity(), "Pedido de normalização Cloro", Toast.LENGTH_SHORT).show();
                        } else {
                            NormalizarCloro.setText(getResources().getString(R.string.button_normalize));
                            NormalizarPH.setBackgroundResource(R.drawable.button_round_corners_normalize);
                            Toast.makeText(getActivity(), "Valor de Cloro demasiado alto para normalizar", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });


            GraphView graph = (GraphView) getActivity().findViewById(R.id.graph_phcloro);

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(18, 6.8),
                    new DataPoint(19, 6.9),
                    new DataPoint(20, 7.3),
                    new DataPoint(21, 7.0),
                    new DataPoint(22, 6.7)
            });

            series.setColor(Color.parseColor("#D37645"));
            graph.addSeries(series);

            LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(18, 3.0),
                    new DataPoint(19, 3.7),
                    new DataPoint(20, 3.3),
                    new DataPoint(21, 2.7),
                    new DataPoint(22, 2.2)
            });

            series1.setColor(Color.parseColor("#E6D341"));
            graph.addSeries(series1);

            /* Gráfico nos Controlos*/

            final ScArcGauge gauge = (ScArcGauge) getActivity().findViewById(R.id.gauge);
            assert gauge != null;

            indicator = (ImageView) getActivity().findViewById(R.id.indicator);
            assert indicator != null;

            indicator.setPivotX(30f);
            indicator.setPivotY(30f);

            gauge.setHighValue(Math.round((100*valorpH)/MAX_PH));

            gauge.setOnEventListener(new ScGauge.OnEventListener() {
                @Override
                public void onValueChange(float lowValue, float highValue) {
                    // Converter percentagem em angulo
                    float angle = gauge.percentageToAngle(highValue);
                    indicator.setRotation(angle);
                }
            });

            final ScArcGauge gauge_cloro = (ScArcGauge) getActivity().findViewById(R.id.gauge_cloro);
            assert gauge_cloro != null;

            indicator_cloro = (ImageView) getActivity().findViewById(R.id.indicator_cloro);
            assert indicator != null;

            indicator_cloro.setPivotX(30f);
            indicator_cloro.setPivotY(30f);

            gauge_cloro.setHighValue(Math.round((100*valorCloro)/MAX_CLORO));

            gauge_cloro.setOnEventListener(new ScGauge.OnEventListener() {
                @Override
                public void onValueChange(float lowValue, float highValue) {
                    float angle = gauge_cloro.percentageToAngle(highValue);
                    indicator_cloro.setRotation(angle);
                }
            });
        }
    }

    public void preencherDados(){

        c_sensor = db.query(false, Contrato.Sensor.TABLE_NAME, Contrato.Sensor.PROJECTION,
                "id_equipamento = ? OR id_equipamento = ?",new String[]{String.valueOf(device_ph),String.valueOf(device_cloro)}, null, null, null, null );

        int i = 0;
        array_equipamento = new int[c_sensor.getCount()];
        array_valor = new float[c_sensor.getCount()];

        if(c_sensor.getCount() > 0){
            c_sensor.moveToFirst();
            while (!c_sensor.isAfterLast()){
                array_equipamento[i] = c_sensor.getInt(1);
                array_valor[i] = c_sensor.getFloat(2);
                c_sensor.moveToNext();
                i++;
            }
        }

        c_historico = db.query(false, Contrato.Historico.TABLE_NAME, Contrato.Historico.PROJECTION,
                "id_equipamento = ? OR id_equipamento = ?",new String[]{String.valueOf(device_ph),String.valueOf(device_cloro)}, null, null, null, null );

        int j=0;
        array_equipamento_historico = new int[c_historico.getCount()];
        array_valor_historico = new int[c_historico.getCount()];

        if(c_historico.getCount() > 0){
            c_historico.moveToFirst();
            while(!c_historico.isAfterLast()){
                array_equipamento_historico[j] = c_historico.getInt(1);
                array_valor_historico[j] = c_historico.getInt(2);
                c_historico.moveToNext();
                j++;
            }
        }
        EscolherDados();
    }

    public void EscolherDados(){
        for(int i=0; i<=(array_equipamento.length-1);i++){
            if(array_equipamento[i] == device_ph){
                valorpH = array_valor[i];
            }
            else if(array_equipamento[i] == device_cloro){
                valorCloro = array_valor[i];
            }
        }

        for(int i=0; i<=(array_equipamento_historico.length-1);i++){
            if(array_equipamento_historico[i] == device_ph){
                valorpHDesejado = array_valor_historico[i];
            }
            else if(array_equipamento_historico[i] == device_cloro){
                valorCloroDesejado = array_valor_historico[i];
            }
        }
        MostrarDados();
    }

    public void MostrarDados(){
        phValue.setText(""+valorpH);
        cloroValue.setText(""+valorCloro);

        if(valorpHDesejado != 0){
            NormalizarPH.setText(getResources().getString(R.string.button_normalized));
            NormalizarPH.setBackgroundResource(R.drawable.button_round_corners_parar);
        }else{
            NormalizarPH.setText(getResources().getString(R.string.button_normalize));
            NormalizarPH.setBackgroundResource(R.drawable.button_round_corners_normalize);
        }

        if(valorCloroDesejado != 0){
            NormalizarCloro.setText(getResources().getString(R.string.button_normalized));
            NormalizarCloro.setBackgroundResource(R.drawable.button_round_corners_parar);
        }else{
            NormalizarCloro.setText(getResources().getString(R.string.button_normalize));
            NormalizarCloro.setBackgroundResource(R.drawable.button_round_corners_normalize);
        }

        editor.putBoolean("Menu", false);
        editor.commit();
    }

    public void EnviarPedidosGET(final int equipamento, final String sensor){
        /* GET */

        editor.putBoolean("Menu", true);
        editor.commit();

        String url_get = "http://www.myapps.shared.town/webservices/ws_get_configuracao.php";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url_get, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray(Utils.param_dados);

                    JSONObject obj_ph = array.getJSONObject(0);
                    JSONObject obj_cloro = array.getJSONObject(1);

                    get_ph = obj_ph.getString("valor");
                    get_cloro = obj_cloro.getString("valor");

                    EnviarPedidosPOST(equipamento, sensor);

                } catch(JSONException ex){}
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),"Erro de ligação.", Toast.LENGTH_LONG).show();
                editor.putBoolean("Menu", false);
                editor.commit();
            }
        });

        MySingleton.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
    }

    public void EnviarPedidosPOST(final int equipamento, final String sensor){

        editor.putBoolean("Menu", true);
        editor.commit();

        /* POST */

        String url_post = "http://www.myapps.shared.town/webservices/ws_insert_historico.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_post, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonoutput = new JSONObject(response);
                } catch (JSONException ex) {
                    ContentValues cv = new ContentValues();
                    if(sensor == "ph") {
                        if(valorpHDesejado == 0){
                            cv.put(Contrato.Historico.COLUMN_VALOR, valorpHDesejado);
                            NormalizarPH.setText(getResources().getString(R.string.button_normalize));
                            NormalizarPH.setBackgroundResource(R.drawable.button_round_corners_normalize);
                        }
                        else {
                            cv.put(Contrato.Historico.COLUMN_VALOR, get_ph);
                            NormalizarPH.setText(getResources().getString(R.string.button_normalized));
                            NormalizarPH.setBackgroundResource(R.drawable.button_round_corners_parar);
                            Toast.makeText(getActivity(), "A Normalizar", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        if(valorCloroDesejado == 0){
                            cv.put(Contrato.Historico.COLUMN_VALOR, valorCloroDesejado);
                            NormalizarCloro.setText(getResources().getString(R.string.button_normalize));
                            NormalizarCloro.setBackgroundResource(R.drawable.button_round_corners_normalize);
                        }
                        else {
                            cv.put(Contrato.Historico.COLUMN_VALOR, get_cloro);
                            NormalizarCloro.setText(getResources().getString(R.string.button_normalized));
                            NormalizarCloro.setBackgroundResource(R.drawable.button_round_corners_parar);
                            Toast.makeText(getActivity(), "A Normalizar", Toast.LENGTH_SHORT).show();
                        }
                    }
                    db.update(Contrato.Historico.TABLE_NAME, cv, Contrato.Historico.COLUMN_IDEQUIPAMENTO + " = ?", new String[]{String.valueOf(equipamento)});
                    preencherDados();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(sensor == "ph"){
                    if(valorpHDesejado == 0)
                        valorpHDesejado = 1;
                    else
                        valorpHDesejado = 0;
                }
                else{
                    if(valorCloroDesejado == 0)
                        valorCloroDesejado = 1;
                    else
                        valorCloroDesejado = 0;
                }
                Toast.makeText(getActivity(),"Erro de ligação.", Toast.LENGTH_SHORT).show();
                editor.putBoolean("Menu", false);
                editor.commit();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id_equipamento", String.valueOf(equipamento));
                //params.put("time", String.valueOf(date));
                if(sensor == "ph") {
                    if(valorpHDesejado == 0)
                        params.put("valor", String.valueOf(valorpHDesejado));
                    else
                        params.put("valor", get_ph);
                }
                else {
                    if(valorCloroDesejado == 0)
                        params.put("valor", String.valueOf(valorCloroDesejado));
                    else
                        params.put("valor", get_cloro);
                }
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
        return inflater.inflate(R.layout.fragment_phcloro, container, false);
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
        // mListener = null;
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
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
