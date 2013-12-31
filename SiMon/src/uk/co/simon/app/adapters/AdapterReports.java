package uk.co.simon.app.adapters;

import java.util.ArrayList;
import java.util.List;

import uk.co.simon.app.sqllite.SQLReport;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterReports extends BaseAdapter {
	
	private LayoutInflater mInflater;
	private List<SQLReport> list = new ArrayList<SQLReport>();

    public AdapterReports(Context context, List<SQLReport> list) {
        mInflater = LayoutInflater.from(context);
        this.list = list;
    }
    
	public int getCount() {
		return list.size();
	}

	public SQLReport getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return list.get(position).getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
		if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(android.R.id.text1);
            holder.sub = (TextView) convertView.findViewById(android.R.id.text2);
		
            convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
 
        SQLReport report = list.get(position);
        holder.title.setText(report.getReportRef());
        holder.sub.setText(report.getReportDate());
        
        return convertView;
	}
	
	static class ViewHolder {
		TextView title;
		TextView sub;
	}
}
