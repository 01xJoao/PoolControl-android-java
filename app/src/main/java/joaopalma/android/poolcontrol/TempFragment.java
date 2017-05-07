package joaopalma.android.poolcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import joaopalma.android.poolcontrol.db.Contrato;
import joaopalma.android.poolcontrol.db.DB;

public class TempFragment extends Fragment {
    // private OnFragmentInteractionListener mListener;
    int temperatura;
    String temperaturaHrs;
    int Valor_tempDesejada;
    int valor_barra;
    int equipamento = 7;
    int maximaTemp = 40;

    ArrayList<Integer> temperaturas;
    ArrayList<String> temperaturasHrs;

    PullRefreshLayout layout;

    /* Base de dados*/

    DB mDbHelper;
    SQLiteDatabase db;
    Cursor c_historico, c_sensor;

    DiscreteSeekBar BarChangeTemp;
    Button buttonAlterarTemp;

    TextView TempView;

    TextView primeiro, segundo, terceiro, quarto, quinto, sexto, setimo, oitavo;
    TextView primeiros, segundos, terceiros, quartos, quintos, sextos, setimos, oitavos;


    SharedPreferences.Editor editor;

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

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        if (isAdded()) {

            temperaturas = new ArrayList<>();
            temperaturasHrs = new ArrayList<>();

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

            primeiro = (TextView) getActivity().findViewById(R.id.primeiro);
            segundo = (TextView) getActivity().findViewById(R.id.segundo);
            terceiro = (TextView) getActivity().findViewById(R.id.terceiro);
            quarto = (TextView) getActivity().findViewById(R.id.quarto);
            quinto = (TextView) getActivity().findViewById(R.id.quinto);
            sexto = (TextView) getActivity().findViewById(R.id.sexto);
            setimo = (TextView) getActivity().findViewById(R.id.setimo);
            oitavo = (TextView) getActivity().findViewById(R.id.oitavo);

            primeiros = (TextView) getActivity().findViewById(R.id._primeiro);
            segundos = (TextView) getActivity().findViewById(R.id._segundo);
            terceiros = (TextView) getActivity().findViewById(R.id._terceiro);
            quartos = (TextView) getActivity().findViewById(R.id._quarto);
            quintos = (TextView) getActivity().findViewById(R.id._quinto);
            sextos = (TextView) getActivity().findViewById(R.id._sexto);
            setimos = (TextView) getActivity().findViewById(R.id._setimo);
            oitavos = (TextView) getActivity().findViewById(R.id._oitavo);

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

                    if(Valor_tempDesejada != temperatura && valor_barra == Valor_tempDesejada){
                        buttonAlterarTemp.setText(getResources().getString(R.string.button_changed));
                        buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners_parar);
                    }

                    if (temperatura == maximaTemp) {
                        BarChangeTemp.setProgress(temperatura);
                        valor_barra = value = temperatura;
                    }
                    return value;
                }
            });

            buttonAlterarTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(temperatura > valor_barra)
                        Toast.makeText(getActivity(), "Não é possivel baixar temperatura.", Toast.LENGTH_SHORT).show();
                    else {
                        if (Valor_tempDesejada != 0 && valor_barra == Valor_tempDesejada) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
                            builder.setTitle("Temperatura em alteração...");
                            builder.setMessage("Deseja cancelar a mudança de temperatura?")
                                    .setCancelable(false)
                                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Valor_tempDesejada = 0;
                                            Toast.makeText(getActivity(), "A cancelar...", Toast.LENGTH_SHORT).show();
                                            enviarPostHistorico();
                                        }
                                    })
                                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {
                            Valor_tempDesejada = valor_barra;
                            Toast.makeText(getActivity(), "A alterar temperatura...", Toast.LENGTH_SHORT).show();
                            enviarPostHistorico();
                        }
                    }
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

        c_sensor = db.query(false, Contrato.Sensor.TABLE_NAME, Contrato.Sensor.PROJECTION,
                "id_equipamento = ?", new String[]{String.valueOf(equipamento)}, null, null, null, null);

        c_sensor.moveToLast();
        temperatura = c_sensor.getInt(2);
        temperaturaHrs = c_sensor.getString(3);

        temperaturas.clear();
        temperaturasHrs.clear();

        for(int i = 0; i<= 6; i++){
            c_sensor.moveToPrevious();
            temperaturas.add(c_sensor.getInt(2));
            temperaturasHrs.add(c_sensor.getString(3));
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mostrarDados();
            }
        }, 10);
    }

    public void mostrarDados(){

        if (temperatura == maximaTemp) {
            BarChangeTemp.setMax(temperatura);
            BarChangeTemp.setProgress(temperatura);
        }
        else {
            BarChangeTemp.setMin(temperatura);

            if (Valor_tempDesejada != 0)
                BarChangeTemp.setProgress(Valor_tempDesejada);
            else {
                BarChangeTemp.setProgress(temperatura);
            }

            if (Valor_tempDesejada > temperatura) {
                buttonAlterarTemp.setText(getResources().getString(R.string.button_changed));
                buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners_parar);
            } else {
                buttonAlterarTemp.setText(getResources().getString(R.string.button_change));
                buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners);
            }
        }
        if (temperatura < 10)
            TempView.setText(" "+temperatura+"ºc ");
        else
            TempView.setText(""+temperatura+"ºc");

        editor.putBoolean("Menu", false);
        editor.commit();

        GraphView graph = (GraphView) getActivity().findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(1, temperaturas.get(3)),
                new DataPoint(2, temperaturas.get(2)),
                new DataPoint(3, temperaturas.get(1)),
                new DataPoint(4, temperaturas.get(0)),
                new DataPoint(5, temperatura)
        });
        graph.addSeries(series);

        primeiro.setText(temperaturasHrs.get(6));
        segundo.setText(temperaturasHrs.get(5));
        terceiro.setText(temperaturasHrs.get(4));
        quarto.setText(temperaturasHrs.get(3));
        quinto.setText(temperaturasHrs.get(2));
        sexto.setText(temperaturasHrs.get(1));
        setimo.setText(temperaturasHrs.get(0));
        oitavo.setText(temperaturaHrs);

        primeiros.setText(""+temperaturas.get(6));
        segundos.setText(""+temperaturas.get(5));
        terceiros.setText(""+temperaturas.get(4));
        quartos.setText(""+temperaturas.get(3));
        quintos.setText(""+temperaturas.get(2));
        sextos.setText(""+temperaturas.get(1));
        setimos.setText(""+temperaturas.get(0));
        oitavos.setText(""+temperatura);

    }

    public void enviarPostHistorico(){
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
                editor.putBoolean("Menu", false);
                editor.commit();
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
