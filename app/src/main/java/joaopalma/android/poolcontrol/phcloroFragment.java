package joaopalma.android.poolcontrol;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class phcloroFragment extends Fragment {
    // private OnFragmentInteractionListener mListener;
    PullRefreshLayout layout;

    int device_ph = 13;
    int device_cloro = 14;

    String get_ph;
    String get_cloro;

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

        if (isAdded()) {

            getActivity().setTitle("pH & Cloro");

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

            Button NormalizarPH = (Button) getActivity().findViewById(R.id.normalizar_ph);
            Button NormalizarCloro = (Button) getActivity().findViewById(R.id.normalizar_cloro);

            NormalizarPH.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnviarPedidosGET(device_ph, "ph");
                    Toast.makeText(getActivity(), "Pedido de normalização pH", Toast.LENGTH_SHORT).show();
                }
            });

            NormalizarCloro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnviarPedidosGET(device_cloro, "cloro");
                    Toast.makeText(getActivity(), "Pedido de normalização Cloro", Toast.LENGTH_SHORT).show();
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

            // Find the components
            final ScArcGauge gauge = (ScArcGauge) this.getActivity().findViewById(R.id.gauge);
            assert gauge != null;

            final ImageView indicator = (ImageView) this.getActivity().findViewById(R.id.indicator);
            assert indicator != null;

            // Set the center pivot for a right rotation
            indicator.setPivotX(30f);
            indicator.setPivotY(30f);

            // If you set the value from the xml that not produce an event so I will change the
            // value from code.
            gauge.setHighValue(30);

            // Each time I will change the value I must write it inside the counter text.
            gauge.setOnEventListener(new ScGauge.OnEventListener() {
                @Override
                public void onValueChange(float lowValue, float highValue) {
                    // Convert the percentage value in an angle
                    float angle = gauge.percentageToAngle(highValue);
                    indicator.setRotation(angle);
                }
            });
        }
    }

    public void EnviarPedidosGET(final int equipamento, final String sensor){
        /* GET */

        String url_get = "https://poolcontrol.000webhostapp.com/webservices/ws_get_configuracao.php";

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
                Toast.makeText(getActivity(),"Erro GET: "+String.valueOf(error), Toast.LENGTH_LONG).show();
            }
        });

        MySingleton.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
    }

    public void EnviarPedidosPOST(final int equipamento, final String sensor){

        /* POST */

        String url_post = "https://poolcontrol.000webhostapp.com/webservices/ws_insert_historico.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_post, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonoutput = new JSONObject(response);
                } catch (JSONException ex) {
                    Toast.makeText(getActivity(),"A Normalizar", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),"Erro POST: "+String.valueOf(error), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id_equipamento", String.valueOf(equipamento));
                //params.put("time", String.valueOf(date));
                if(sensor == "ph")
                    params.put("valor", get_ph);
                else
                    params.put("valor", get_cloro);
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
