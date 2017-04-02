package joaopalma.android.poolcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
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

public class LightsFragment extends Fragment {
    // private OnFragmentInteractionListener mListener;
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

    DB mDbHelper;
    SQLiteDatabase db;

    public LightsFragment() {
    }

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

        if (isAdded()) {
            getActivity().setTitle("Luzes");

            final Button btCentral = (Button) getActivity().findViewById(R.id.LightCentral);
            final Button btSmall1 = (Button) getActivity().findViewById(R.id.LightSmall1);
            final Button btSmall2 = (Button) getActivity().findViewById(R.id.LightSmall2);
            final Button btSmall3 = (Button) getActivity().findViewById(R.id.LightSmall3);
            final Button btSmall4 = (Button) getActivity().findViewById(R.id.LightSmall4);

            btCentral.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnviarPedidos(LuzCentral, lightCentralBool);
                    if (lightCentralBool) {
                        btCentral.setBackgroundResource(R.drawable.button_round_corners_light_on);
                        lightCentralBool = !lightCentralBool;
                    } else {
                        btCentral.setBackgroundResource(R.drawable.button_round_corners_light_off);
                        lightCentralBool = !lightCentralBool;
                    }
                }
            });

            btSmall1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnviarPedidos(LuzFrenteEsq, lightSmall1Bool);
                    if (lightSmall1Bool) {
                        btSmall1.setBackgroundResource(R.drawable.button_round_corners_light_on);
                        lightSmall1Bool = !lightSmall1Bool;
                    } else {
                        btSmall1.setBackgroundResource(R.drawable.button_round_corners_light_off);
                        lightSmall1Bool = !lightSmall1Bool;
                    }
                }
            });

            btSmall2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnviarPedidos(LuzFrenteDir, lightSmall2Bool);
                    if (lightSmall2Bool) {
                        btSmall2.setBackgroundResource(R.drawable.button_round_corners_light_on);
                        lightSmall2Bool = !lightSmall2Bool;
                    } else {
                        btSmall2.setBackgroundResource(R.drawable.button_round_corners_light_off);
                        lightSmall2Bool = !lightSmall2Bool;
                    }
                }
            });

            btSmall3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnviarPedidos(LuzTrasEsq, lightSmall3Bool);
                    if (lightSmall3Bool) {
                        btSmall3.setBackgroundResource(R.drawable.button_round_corners_light_on);
                        lightSmall3Bool = !lightSmall3Bool;
                    } else {
                        btSmall3.setBackgroundResource(R.drawable.button_round_corners_light_off);
                        lightSmall3Bool = !lightSmall3Bool;
                    }
                }
            });

            btSmall4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnviarPedidos(LuzTrasDir, lightSmall4Bool);
                    if (lightSmall4Bool) {
                        btSmall4.setBackgroundResource(R.drawable.button_round_corners_light_on);
                        lightSmall4Bool = !lightSmall4Bool;
                    } else {
                        btSmall4.setBackgroundResource(R.drawable.button_round_corners_light_off);
                        lightSmall4Bool = !lightSmall4Bool;
                    }
                }
            });
        }
    }

    public void EnviarPedidos(final int equipamento, final boolean light){
        //Toast.makeText(getActivity(),"Temperatura desejada " + Valor_TemperaturaDesejada + "º", Toast.LENGTH_SHORT).show();

        final int mylight = (light) ? 1 : 0;

        String url = "https://poolcontrol.000webhostapp.com/webservices/ws_insert_historico.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonoutput = new JSONObject(response);
                    //Toast.makeText(getActivity(), jsonoutput.getString(Utils.param_status), Toast.LENGTH_SHORT).show();
                } catch (JSONException ex) {
                    ContentValues cv = new ContentValues();
                    cv.put(Contrato.Historico.COLUMN_VALOR, mylight);
                    db.update(Contrato.Historico.TABLE_NAME, cv, Contrato.Historico.COLUMN_IDEQUIPAMENTO + " = ?", new String[]{String.valueOf(equipamento)});
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),"Erro: "+String.valueOf(error), Toast.LENGTH_LONG).show();
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
