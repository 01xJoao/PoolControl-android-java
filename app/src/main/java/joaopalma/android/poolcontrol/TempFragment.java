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
    int temperatura = 20;
    int Valor_TemperaturaDesejada;
    int equipamento = 7;

    PullRefreshLayout layout;

    /* Base de dados*/

    DB mDbHelper;
    SQLiteDatabase db;
    Cursor c_historico;

    DiscreteSeekBar BarChangeTemp;
    Button buttonAlterarTemp;

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

            getActivity().setTitle("Temperatura");

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
                    buttonAlterarTemp.setText("ALTERAR");
                    buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners);
                    Valor_TemperaturaDesejada = value;
                    return value;
                }
            });

            buttonAlterarTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "https://poolcontrol.000webhostapp.com/webservices/ws_insert_historico.php";
                    Toast.makeText(getActivity(), "A alterar temperatura...", Toast.LENGTH_SHORT).show();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonoutput = new JSONObject(response);
                                //Toast.makeText(getActivity(), jsonoutput.getString(Utils.param_status), Toast.LENGTH_SHORT).show();
                            } catch (JSONException ex) {
                                ContentValues cv = new ContentValues();
                                cv.put(Contrato.Historico.COLUMN_VALOR, Valor_TemperaturaDesejada);
                                db.update(Contrato.Historico.TABLE_NAME, cv, Contrato.Historico.COLUMN_IDEQUIPAMENTO + " = ?", new String[]{String.valueOf(equipamento)});
                                if(Valor_TemperaturaDesejada > temperatura) {
                                    Toast.makeText(getActivity(), "Temperatura desejada " + Valor_TemperaturaDesejada + "º", Toast.LENGTH_SHORT).show();
                                    buttonAlterarTemp.setText("ALTERANDO");
                                    buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners_parar);
                                }
                                else {
                                    Toast.makeText(getActivity(), "Temperatura atual.", Toast.LENGTH_SHORT).show();
                                    buttonAlterarTemp.setText("ALTERAR");
                                    buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners);
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getActivity(), "Erro: " + String.valueOf(error), Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("id_equipamento", String.valueOf(equipamento));
                            params.put("valor", String.valueOf(Valor_TemperaturaDesejada));
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
            });

            /* RECEBER TEMPERATURA */

            preencherDados();
        }

    }

    public void preencherDados(){
        c_historico = db.query(false, Contrato.Historico.TABLE_NAME, Contrato.Historico.PROJECTION,
                "id_equipamento = ?", new String[]{String.valueOf(equipamento)}, null, null, null, null);
        c_historico.moveToFirst();
        Valor_TemperaturaDesejada = c_historico.getInt(2);
        mostrarDados();
    }

    public void mostrarDados(){
        BarChangeTemp.setProgress(Valor_TemperaturaDesejada);

        if(Valor_TemperaturaDesejada > temperatura) {
            buttonAlterarTemp.setText("ALTERANDO");
            buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners_parar);
        }
        else {
            buttonAlterarTemp.setText("ALTERAR");
            buttonAlterarTemp.setBackgroundResource(R.drawable.button_round_corners);
        }
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
