package joaopalma.android.poolcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class Adapter extends ArrayAdapter<String> {
    String[] enginesHrs;
    String[] enginesDuration;

    public Adapter(Context context, String[] engines, String[] enginesDuration) {
        super(context, R.layout.list_automatic_engine, engines);
        this.enginesHrs = engines;
        this.enginesDuration = enginesDuration;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_automatic_engine, null);
        }

        TextView engineHrsTV = (TextView) v.findViewById(R.id.engine_tv_hrs);
        TextView engineDurationTV = (TextView) v.findViewById(R.id.engine_tv_duration);

        engineHrsTV.setText(enginesHrs[position]);
        engineDurationTV.setText(enginesDuration[position]);

        return v;
    }
}
