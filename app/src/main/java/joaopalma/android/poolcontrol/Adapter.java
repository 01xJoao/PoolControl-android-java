package joaopalma.android.poolcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import joaopalma.android.poolcontrol.db.Contrato;
import joaopalma.android.poolcontrol.db.DB;

public class Adapter extends ArrayAdapter<String> {
    ArrayList<String> enginesHrs;
    ArrayList<String> enginesDuration;
    DB mDbHelper = new DB(getContext());
    SQLiteDatabase db = mDbHelper.getReadableDatabase();
    int equipamento;

    public Adapter(Context context,int equipamento, ArrayList<String> engines, ArrayList<String> enginesDuration) {
        super(context, R.layout.list_automatic_engine, engines);
        this.equipamento = equipamento;
        this.enginesHrs = engines;
        this.enginesDuration = enginesDuration;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_automatic_engine, null);
        }

        TextView engineHrsTV = (TextView) v.findViewById(R.id.tv_hrs);
        TextView engineDurationTV = (TextView) v.findViewById(R.id.tv_duration);
        Button btnRemove = (Button) v.findViewById(R.id.btn_remove);

        engineHrsTV.setText(enginesHrs.get(position));
        engineDurationTV.setText(enginesDuration.get(position));

        btnRemove.setTag(position);
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"A Remover...",Toast.LENGTH_SHORT).show();
                removeFromDB(position);
            }
        });
        return v;
    }

    public void removeFromDB(final int position){

        String url = "http://www.myapps.shared.town/webservices/ws_delete_agendamento.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonoutput = new JSONObject(response);
                } catch (JSONException ex) {
                    Toast.makeText(getContext(),"Removido",Toast.LENGTH_SHORT).show();
                    db.delete(Contrato.Agendamento.TABLE_NAME,"id_equipamento = ? and inicio = ?",new String[]{Integer.toString(equipamento),enginesHrs.get(position)});
                    enginesHrs.remove(position);
                    enginesDuration.remove(position);
                    notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(),"Erro",Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id_equipamento", String.valueOf(equipamento));
                params.put("inicio", enginesHrs.get(position));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        MySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }
}
