package uk.co.simon.app.adapters;

import java.util.ArrayList;
import java.util.List;

import uk.co.simon.app.R;
import uk.co.simon.app.sqllite.DataSourceLocations;
import uk.co.simon.app.sqllite.SQLLocation;
import uk.co.simon.app.sqllite.SQLReportItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterReportItems extends BaseAdapter {
	
	private LayoutInflater mInflater;
	private List<SQLReportItem> list = new ArrayList<SQLReportItem>();
	private DataSourceLocations locationdatasource;
	private Context context;

    public AdapterReportItems(Context context, List<SQLReportItem> list) {
        mInflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
    }
    
	public int getCount() {
		return list.size();
	}

	public SQLReportItem getItem(int position) {
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
        
        SQLReportItem reportItem = list.get(position);
        holder.title.setText(reportItem.getReportItem());
        if (reportItem.getLocationId()<1) {
        	holder.sub.setText(context.getResources().getString(R.string.dailyProgressAddItemInstruction));
        } else {
        	locationdatasource = new DataSourceLocations(context);
        	locationdatasource.open();
        	SQLLocation location = locationdatasource.getLocation(reportItem.getLocationId());
        	holder.sub.setText(location.getLocation());
        	locationdatasource.close();
        }
        return convertView;
	}
	
	static class ViewHolder {
		TextView title;
		TextView sub;
	}
}
