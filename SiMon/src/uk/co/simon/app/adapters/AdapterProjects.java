package uk.co.simon.app.adapters;

import java.util.ArrayList;
import java.util.List;

import uk.co.simon.app.sqllite.SQLProject;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterProjects extends BaseAdapter {
	
	private LayoutInflater mInflater;
	private List<SQLProject> list = new ArrayList<SQLProject>();

    public AdapterProjects(Context context, List<SQLProject> list) {
        mInflater = LayoutInflater.from(context);
        this.list = list;
    }
    
	public int getCount() {
		return list.size();
	}

	public SQLProject getItem(int position) {
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
 
        SQLProject report = list.get(position);
        holder.title.setText(report.getProject());
        holder.sub.setText(report.getProjectNumber());
        
        return convertView;
	}
	
	static class ViewHolder {
		TextView title;
		TextView sub;
	}
}
