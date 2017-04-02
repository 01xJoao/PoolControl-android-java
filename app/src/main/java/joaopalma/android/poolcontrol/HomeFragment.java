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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import joaopalma.android.poolcontrol.db.Contrato;
import joaopalma.android.poolcontrol.db.DB;

public class HomeFragment extends Fragment {
    // private OnFragmentInteractionListener mListener;
    PullRefreshLayout layout;

    int cobertura = 1;
    int luzCentral = 8;

    int valorLuzCentral;
    int valorCobertura;

    SwitchCompat switchLight;
    SwitchCompat switchCobertura;

    DB mDbHelper;
    SQLiteDatabase db;
    Cursor c_historico;

    int array_equipamento[];
    int array_valor[];

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

        getActivity().setTitle("PoolControl");

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

        mDbHelper = new DB(getActivity());
        db = mDbHelper.getReadableDatabase();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                preencherDados();
            }
        }, 1500);
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
        EscolherDados();
    }

    public void  EscolherDados(){

        for(int i=0; i<=(array_equipamento.length-1);i++){
            if(array_equipamento[i] == cobertura){
                valorCobertura = array_valor[i];
            }
            if(array_equipamento[i] == luzCentral){
                valorLuzCentral = array_valor[i];
            }
        }
        MostrarDados();
    }

    public void MostrarDados(){
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
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),"Erro: " + String.valueOf(error), Toast.LENGTH_LONG).show();
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
