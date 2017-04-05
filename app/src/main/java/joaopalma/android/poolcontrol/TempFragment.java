package joaopalma.android.poolcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.widget.PullRefreshLayout;
import com.baoyz.widget.SmartisanDrawable;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import joaopalma.android.poolcontrol.db.Contrato;
import joaopalma.android.poolcontrol.db.DB;

public class TempFragment extends Fragment {
    // private OnFragmentInteractionListener mListener;
    int temperatura;
    int Valor_tempDesejada;
    int valor_barra;
    int equipamento = 7;

    PullRefreshLayout layout;

    /* Base de dados*/

    DB mDbHelper;
    SQLiteDatabase db;
    Cursor c_historico, c_sensor;

    DiscreteSeekBar BarChangeTemp;
    Button buttonAlterarTemp;

    TextView TempView;

    public TempFragment() {
    }

    public static TempFragment newInstance() {
        TempFragment fragment = new TempFragment();
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

        if (isAdded()) {

            getActivity().setTitle(getResources().getString(R.string.title_temp));

            TempView = (TextView) getActivity().findViewById(R.id.temp_graus);

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

            final TextView controlView = (TextView) getActivity().findViewById(R.id.tview_temp_control);
            final LinearLayout controlLayout = (LinearLayout) getActivity().findViewById(R.id.card_control_layout);
            final TextView historyView = (TextView) getActivity().findViewById(R.id.tview_temp_history);
            final LinearLayout historyLayout = (LinearLayout) getActivity().findViewById(R.id.card_history_layout);

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

            GraphView graph = (GraphView) getActivity().findViewById(R.id.graph);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(0, 1),
                    new DataPoint(1, 5),
                    new DataPoint(2, 3),
                    new DataPoint(3, 2),
                    new DataPoint(4, 6)
            });
            graph.addSeries(series);

            /* ALTERAR TEMPERATURA */

            buttonAlterarTemp = (Button) getActivity().findViewById(R.id.alterarTemperatura);

            /* Barra da temperatura */

            BarChangeTemp = (DiscreteSeekBar) getActivity().findViewById(R.id.ChangeTemp);
            BarChangeTemp.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
                @Override
                public int transform(int value) {
                    buttonAlterarTemp.setText(getResources().getString(R.string.button_change));
                    buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners);
                    valor_barra = value;
                    return value;
                }
            });

            buttonAlterarTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Valor_tempDesejada != 0){
                        Valor_tempDesejada = 0;
                        Toast.makeText(getActivity(), "A cancelar...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Valor_tempDesejada = valor_barra;
                        Toast.makeText(getActivity(), "A alterar temperatura...", Toast.LENGTH_SHORT).show();
                    }
                    enviarPostHistorico();
                }
            });

            preencherDados();
        }

    }

    public void preencherDados(){
        c_historico = db.query(false, Contrato.Historico.TABLE_NAME, Contrato.Historico.PROJECTION,
                "id_equipamento = ?", new String[]{String.valueOf(equipamento)}, null, null, null, null);
        c_historico.moveToFirst();
        Valor_tempDesejada = c_historico.getInt(2);

        /*c_sensor = db.query(false, Contrato.Sensor.TABLE_NAME, Contrato.Sensor.PROJECTION,
                "id_equipamento = ?", new String[]{String.valueOf(equipamento)}, null, null, Contrato.Sensor._ID + " DESC", "1");*/

        c_sensor = db.query(false, Contrato.Sensor.TABLE_NAME, Contrato.Sensor.PROJECTION,
                "id_equipamento = ?", new String[]{String.valueOf(equipamento)}, null, null, null, null);

        c_sensor.moveToLast();
        temperatura = c_sensor.getInt(2);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mostrarDados();
            }
        }, 100);
    }

    public void mostrarDados(){
        BarChangeTemp.setMin(temperatura);

        if(Valor_tempDesejada != 0)
            BarChangeTemp.setProgress(Valor_tempDesejada);
        else
            BarChangeTemp.setProgress(temperatura);

        if(Valor_tempDesejada > temperatura) {
            buttonAlterarTemp.setText(getResources().getString(R.string.button_changed));
            buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners_parar);
        }
        else {
            buttonAlterarTemp.setText(getResources().getString(R.string.button_change));
            buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners);
        }

        TempView.setText(""+temperatura+"ºc");

    }

    public void enviarPostHistorico(){
        String url = "http://www.myapps.shared.town/webservices/ws_insert_historico.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonoutput = new JSONObject(response);
                    //Toast.makeText(getActivity(), jsonoutput.getString(Utils.param_status), Toast.LENGTH_SHORT).show();
                } catch (JSONException ex) {
                    ContentValues cv = new ContentValues();

                    if(Valor_tempDesejada == 0){
                        Toast.makeText(getActivity(), "Alteração cancelada.", Toast.LENGTH_SHORT).show();
                        cv.put(Contrato.Historico.COLUMN_VALOR, Valor_tempDesejada);
                        db.update(Contrato.Historico.TABLE_NAME, cv, Contrato.Historico.COLUMN_IDEQUIPAMENTO + " = ?", new String[]{String.valueOf(equipamento)});
                    }
                    else {
                        if (Valor_tempDesejada > temperatura) {
                            cv.put(Contrato.Historico.COLUMN_VALOR, Valor_tempDesejada);
                            db.update(Contrato.Historico.TABLE_NAME, cv, Contrato.Historico.COLUMN_IDEQUIPAMENTO + " = ?", new String[]{String.valueOf(equipamento)});

                            Toast.makeText(getActivity(), "Temperatura desejada " + Valor_tempDesejada + "º", Toast.LENGTH_SHORT).show();
                            buttonAlterarTemp.setText(getResources().getString(R.string.button_changed));
                            buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners_parar);
                        } else {
                            Toast.makeText(getActivity(), "Temperatura atual.", Toast.LENGTH_SHORT).show();
                            buttonAlterarTemp.setText(getResources().getString(R.string.button_change));
                            buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners);
                        }
                    }
                    preencherDados();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Erro de ligação.", Toast.LENGTH_LONG).show();
                if(Valor_tempDesejada == 0)
                    Valor_tempDesejada = 1;
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                if(Valor_tempDesejada > temperatura || Valor_tempDesejada == 0) {
                    params.put("id_equipamento", String.valueOf(equipamento));
                    params.put("valor", String.valueOf(Valor_tempDesejada));
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
        return inflater.inflate(R.layout.fragment_temp, container, false);
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
