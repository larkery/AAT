package ch.bailu.aat.views;

import android.content.Intent;
import android.database.DataSetObserver;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import ch.bailu.aat.activities.ActivitySwitcher;
import ch.bailu.aat.activities.NodeDetailActivity;
import ch.bailu.aat.gpx.GpxInformation;
import ch.bailu.aat.gpx.GpxList;
import ch.bailu.aat.gpx.GpxListArray;
import ch.bailu.aat.helpers.AppTheme;
import ch.bailu.aat.services.ServiceContext;
import ch.bailu.aat.views.description.TrackDescriptionView;

public class NodeListView extends TrackDescriptionView {


    private final MyWayListView list;


    public NodeListView(ServiceContext scontext, String key, int infoID) {
        super(scontext.getContext(), key, infoID);

        list = new MyWayListView(scontext);
        AppTheme.themify(list, AppTheme.getHighlightColor());

        addView(list);
    }


    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        // As big as possible
        wSpec  = MeasureSpec.makeMeasureSpec (MeasureSpec.getSize(wSpec),  MeasureSpec.EXACTLY);
        hSpec  = MeasureSpec.makeMeasureSpec (MeasureSpec.getSize(hSpec),  MeasureSpec.EXACTLY);

        list.measure(wSpec, hSpec);
        setMeasuredDimension(MeasureSpec.getSize(wSpec), MeasureSpec.getSize(hSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        list.layout(0, 0, r-l, b-t);
    }


    @Override
    public void onContentUpdated(GpxInformation info) {
        list.update(info);
    }



    private class MyWayListView extends ListView
    implements ListAdapter, android.widget.AdapterView.OnItemClickListener {
        private final SparseArray<DataSetObserver> observer= new SparseArray<>(5);

        private GpxListArray array = new GpxListArray(GpxList.NULL_ROUTE);
        private GpxInformation info = GpxInformation.NULL;
        
        private final ServiceContext scontext;

        
        public MyWayListView(ServiceContext sc) {
            super(sc.getContext());
            
            scontext=sc;
            setAdapter(this);
            setOnItemClickListener(this);
        }



        public void update(final GpxInformation info) {
            if (filter.pass(info)) {
                this.array = new GpxListArray(info.getGpxList());
                this.info = info;
                notifyDataSetChanged();
            }
            
        }


        private void notifyDataSetChanged() {
            for (int i=0; i<observer.size(); i++) {
                observer.valueAt(i).onChanged();
            }
        }




        @Override
        public int getCount() {
            return array.size();
        }


        @Override
        public View getView(int position, View recycle, ViewGroup parent) {

            NodeEntryView entry = (NodeEntryView) recycle;

            if (entry == null) {
                entry = new NodeEntryView(scontext, filter.id);
            } 

            entry.update(info, array.get(position));
            return entry;
        }




        @Override
        public Object getItem(int position) {
            return array.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
            final Intent intent = new Intent();
            intent.putExtra("I", pos);
            intent.putExtra("ID", info.getPath());
            ActivitySwitcher.start(getContext(), NodeDetailActivity.class, intent);

        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return getCount()==0;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver o) {
            observer.put(o.hashCode(), o);
            notifyDataSetChanged();
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver o) {
            observer.delete(o.hashCode());
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }
    }
}
