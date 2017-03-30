package joaopalma.android.poolcontrol;

import android.content.Context;
import android.content.SharedPreferences;
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

public class HomeFragment extends Fragment {
    // private OnFragmentInteractionListener mListener;
    PullRefreshLayout layout;

    int cobertura = 1;
    int luzCentral = 8;

    String d_cobertura = "d_1";
    String d_luzCentral = "d_8";

    int valorLuzCentral = 0;
    int valorCobertura = 0;

    SwitchCompat switchLight;
    SwitchCompat switchCobertura;

    ArrayList ArrayEquipamento;
    ArrayList<Integer> ArrayValor;

    boolean actualizado = false;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayEquipamento = new ArrayList();
        ArrayValor = new ArrayList<Integer>();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!actualizado) {
                    ActualizarDados();
                    actualizado = true;
                }
            }
        }, 2000);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("PoolControl");

        /* SHARED PREF*/
        if (isAdded()) {

                layout = (PullRefreshLayout) getActivity().findViewById(R.id.swipeRefreshLayout);
                layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        layout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ActualizarDados();
                                layout.setRefreshing(false);
                            }
                        }, 3000);
                    }
                });
                layout.setColorSchemeColors(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
                layout.setRefreshDrawable(new SmartisanDrawable(getActivity(), layout));

                switchCobertura = (SwitchCompat) getActivity().findViewById(R.id.switch_cobertura);
                switchCobertura.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int myInt = (isChecked) ? 1 : 0;
                        EnviarPedido(cobertura, myInt);
                    }
                });

                switchLight = (SwitchCompat) getActivity().findViewById(R.id.switch_light);
                switchLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int myInt = (isChecked) ? 1 : 0;
                        EnviarPedido(luzCentral, myInt);
                    }
                });
        }
        /* Actualizar DADOS*/

        //ActualizarDados();
    }

    public void  ActualizarDados(){
        Toast.makeText(getActivity(),"Dados Carregados", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        ArrayEquipamento.clear();
        ArrayValor.clear();

        valorLuzCentral = 0;
        valorCobertura = 0;

        int size = sharedPref.getInt("equipamentos" + "_size", 0);

        for (int i = 0; i < size; i++) {
            ArrayEquipamento.add(sharedPref.getString("id_equipamento" + "_" + i, null));
            ArrayValor.add(sharedPref.getInt("valor" + "_" + i, 0));
        }

        for(int i=0; i<=(ArrayEquipamento.size()-1);i++){
            if(ArrayEquipamento.get(i).equals(d_cobertura)){
                valorCobertura = ArrayValor.get(i);
            }
            if(ArrayEquipamento.get(i).equals(d_luzCentral)){
                valorLuzCentral = ArrayValor.get(i);
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
        else {
            switchCobertura.setChecked(false);
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
                //params.put("time", String.valueOf(date));
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
