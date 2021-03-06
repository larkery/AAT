package ch.bailu.aat.activities;


import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import java.io.File;

import ch.bailu.aat.R;
import ch.bailu.aat.description.ContentDescription;
import ch.bailu.aat.description.DistanceDescription;
import ch.bailu.aat.description.OnContentUpdatedInterface;
import ch.bailu.aat.description.TrackSizeDescription;
import ch.bailu.aat.dispatcher.ContentSource;
import ch.bailu.aat.dispatcher.CurrentLocationSource;
import ch.bailu.aat.dispatcher.IteratorSource;
import ch.bailu.aat.dispatcher.OverlaySource;
import ch.bailu.aat.dispatcher.RootDispatcher;
import ch.bailu.aat.gpx.GpxInformation;
import ch.bailu.aat.helpers.FileAction;
import ch.bailu.aat.menus.FileMenu;
import ch.bailu.aat.preferences.SolidDirectoryQuery;
import ch.bailu.aat.services.directory.Iterator;
import ch.bailu.aat.services.directory.IteratorSimple;
import ch.bailu.aat.views.ContentView;
import ch.bailu.aat.views.ControlBar;
import ch.bailu.aat.views.DbSynchronizerBusyIndicator;
import ch.bailu.aat.views.GpxListView;
import ch.bailu.aat.views.MainControlBar;
import ch.bailu.aat.views.description.MultiView;
import ch.bailu.aat.views.map.OsmInteractiveView;
import ch.bailu.aat.views.map.overlay.CurrentLocationOverlay;
import ch.bailu.aat.views.map.overlay.OsmOverlay;
import ch.bailu.aat.views.map.overlay.control.FileControlBar;
import ch.bailu.aat.views.map.overlay.control.InformationBarOverlay;
import ch.bailu.aat.views.map.overlay.control.NavigationBarOverlay;
import ch.bailu.aat.views.map.overlay.gpx.GpxDynOverlay;
import ch.bailu.aat.views.map.overlay.gpx.GpxOverlayListOverlay;
import ch.bailu.aat.views.map.overlay.grid.GridDynOverlay;
import ch.bailu.aat.views.preferences.SolidDirectoryMenuButton;
import ch.bailu.aat.views.preferences.TitleView;
import ch.bailu.aat.views.preferences.VerticalScrollView;


public abstract class AbsGpxListActivity extends AbsDispatcher implements OnItemClickListener {

    private FileMenu fileMenu;
    private String                      solid_key;

    private Iterator                    iteratorSimple = Iterator.NULL;

    private SolidDirectoryQuery sdirectory;

    private MultiView                   multiView;


    private GpxListView                 listView;
    private DbSynchronizerBusyIndicator busyControl;


    public abstract void                   displayFile();
    public abstract File                   getDirectory();
    public abstract String                 getLabel();
    public abstract ContentDescription[]   getGpxListItemData();
    public abstract ContentDescription[]   getSummaryData();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sdirectory = new SolidDirectoryQuery(this);
        sdirectory.setValue(getDirectory().getAbsolutePath());
        solid_key = AbsGpxListActivity.class.getSimpleName() +  "_" + sdirectory.getValueAsString();

        multiView = createMultiView();

        final LinearLayout contentView = new ContentView(this);
        contentView.addView(createControlBar(multiView));
        contentView.addView(multiView);
        setContentView(contentView);

        createDispatcher();
    }


    private ControlBar createControlBar(MultiView multiView) {
        final MainControlBar bar = new MainControlBar(getServiceContext());

        busyControl = new DbSynchronizerBusyIndicator(bar.getMenu());

        bar.addAll(multiView);
        bar.add(new SolidDirectoryMenuButton(sdirectory));

        return bar;
    }


    private MultiView createMultiView() {
        final String summary_label = getString(R.string.label_summary);
        final String filter_label = getString(R.string.label_filter);
        final String map_label = getString(R.string.intro_map);
        final String list_label = getString(R.string.label_list);

        final ContentDescription summary_content[] = getSummaryData();
        final ContentDescription filter_content[] = {
                new TrackSizeDescription(this),
                new DistanceDescription(this)
        };

        final VerticalScrollView filter= new VerticalScrollView(this);
        final VerticalScrollView summary= new VerticalScrollView(this);

        final OsmInteractiveView map = new OsmInteractiveView(getServiceContext(), solid_key);

        final OsmOverlay overlayList[] = {
                new GpxOverlayListOverlay(map, getServiceContext()),
                new GpxDynOverlay(map, getServiceContext(), GpxInformation.ID.INFO_ID_LIST_SUMMARY),
                new FileControlBar(map, this),
                new CurrentLocationOverlay(map),
                new GridDynOverlay(map, getServiceContext()),
                new NavigationBarOverlay(map),
                new InformationBarOverlay(map),
        };
        map.setOverlayList(overlayList);


        listView = new GpxListView(this, getGpxListItemData());
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);


        filter.add(new TitleView(this, getLabel()+ " - " + filter_label));
        filter.addAllFilterViews(map.map);


        summary.add(new TitleView(this, getLabel() + " - " + summary_label));

        multiView = new MultiView(this, solid_key, GpxInformation.ID.INFO_ID_ALL);

        multiView.add(listView, list_label);
        multiView.add(map, map, map_label);
        multiView.add(filter,
                filter.addAllContent(filter_content, GpxInformation.ID.INFO_ID_LIST_SUMMARY),
                filter_label);
        multiView.add(summary, summary.addAllContent(summary_content,
                GpxInformation.ID.INFO_ID_LIST_SUMMARY),
                summary_label);

        return multiView;
    }




    private void createDispatcher() {


        final OnContentUpdatedInterface[] target = new OnContentUpdatedInterface[] {
                multiView, this
        };

        final IteratorSource summary = new IteratorSource.Summary(getServiceContext());

        ContentSource[] source = new ContentSource[] {
                new OverlaySource(getServiceContext()),
                new CurrentLocationSource(getServiceContext()),
                summary
        };

        setDispatcher(new RootDispatcher(source, target));

    }




    @Override
    public void onResumeWithService() {
        iteratorSimple = new IteratorSimple(getServiceContext());
        listView.setAdapter(getServiceContext(), iteratorSimple);
        listView.setSelection(sdirectory.getPosition().getValue());

        super.onResumeWithService();
    }


    @Override
    public void onPauseWithService() {
        //sdirectory.setPosition(iteratorSimple.getPosition());

        iteratorSimple.close();
        iteratorSimple = Iterator.NULL;
        listView.setAdapter(getServiceContext(), iteratorSimple);

        super.onPauseWithService();
    }


    @Override
    public void onDestroy() {
        busyControl.close();
        super.onDestroy();
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

        displayFileOnPosition(position);
    }


    private void displayFileOnPosition(int position) {
        sdirectory.getPosition().setValue(position);
        displayFile();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        int position =
                ((AdapterView.AdapterContextMenuInfo)menuInfo).position;

        iteratorSimple.moveToPosition(position);

        fileMenu = new FileMenu(new FileAction(this, iteratorSimple));
        fileMenu.inflateWithHeader(menu);
        fileMenu.prepare(menu);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return fileMenu.onItemClick(item);
    }

}

