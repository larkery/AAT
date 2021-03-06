package ch.bailu.aat.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import ch.bailu.aat.R;
import ch.bailu.aat.description.AltitudeDescription;
import ch.bailu.aat.description.AverageSpeedDescription;
import ch.bailu.aat.description.ContentDescription;
import ch.bailu.aat.description.CurrentSpeedDescription;
import ch.bailu.aat.description.OnContentUpdatedInterface;
import ch.bailu.aat.description.DistanceDescription;
import ch.bailu.aat.description.MaximumSpeedDescription;
import ch.bailu.aat.description.TimeDescription;
import ch.bailu.aat.dispatcher.ContentSource;
import ch.bailu.aat.dispatcher.CurrentLocationSource;
import ch.bailu.aat.dispatcher.EditorSource;
import ch.bailu.aat.dispatcher.OverlaySource;
import ch.bailu.aat.dispatcher.RootDispatcher;
import ch.bailu.aat.dispatcher.TrackerSource;
import ch.bailu.aat.gpx.GpxInformation;
import ch.bailu.aat.services.editor.EditorHelper;
import ch.bailu.aat.views.ContentView;
import ch.bailu.aat.views.ControlBar;
import ch.bailu.aat.views.MainControlBar;
import ch.bailu.aat.views.MvNextButton;
import ch.bailu.aat.views.description.MultiView;
import ch.bailu.aat.views.description.TrackerStateButton;
import ch.bailu.aat.views.description.VerticalView;
import ch.bailu.aat.views.description.CockpitView;
import ch.bailu.aat.views.description.TrackDescriptionView;
import ch.bailu.aat.views.graph.DistanceAltitudeGraphView;
import ch.bailu.aat.views.graph.DistanceSpeedGraphView;
import ch.bailu.aat.views.map.OsmInteractiveView;
import ch.bailu.aat.views.map.overlay.CurrentLocationOverlay;
import ch.bailu.aat.views.map.overlay.OsmOverlay;
import ch.bailu.aat.views.map.overlay.control.EditorOverlay;
import ch.bailu.aat.views.map.overlay.control.InformationBarOverlay;
import ch.bailu.aat.views.map.overlay.control.NavigationBarOverlay;
import ch.bailu.aat.views.map.overlay.gpx.GpxDynOverlay;
import ch.bailu.aat.views.map.overlay.gpx.GpxOverlayListOverlay;
import ch.bailu.aat.views.map.overlay.grid.GridDynOverlay;

public class TrackerActivity extends AbsDispatcher implements OnClickListener{
 
    private static final String SOLID_KEY="tracker";

    private ImageButton          activityCycle;
    private TrackerStateButton   trackerState;
    private MultiView            multiView;
    private OsmInteractiveView   map;
    
    private EditorHelper         edit;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        edit = new EditorHelper(getServiceContext());

        ViewGroup contentView = new ContentView(this);

        multiView = createMultiView();
        contentView.addView(createButtonBar(multiView));
        contentView.addView(multiView);
        setContentView(contentView);

        createDispatcher();
    }


    private MultiView createMultiView() {
        ContentDescription[] data = new ContentDescription[] { 
                new CurrentSpeedDescription(this),
                new AltitudeDescription(this),
                new TimeDescription(this),
                new DistanceDescription(this),
                new AverageSpeedDescription(this),
                new MaximumSpeedDescription(this),
        };

        CockpitView cockpit = new CockpitView(this, SOLID_KEY, GpxInformation.ID.INFO_ID_TRACKER, data);

        map = new OsmInteractiveView(getServiceContext(), SOLID_KEY);

        MultiView mv = new MultiView(this, SOLID_KEY, GpxInformation.ID.INFO_ID_ALL);
        mv.addT(cockpit);
        mv.addT(map);
        mv.addT(new VerticalView(this, SOLID_KEY, GpxInformation.ID.INFO_ID_TRACKER, new TrackDescriptionView[] {
                new DistanceAltitudeGraphView(this, SOLID_KEY),
                new DistanceSpeedGraphView(this, SOLID_KEY)}));
        return mv;
    }

    private ControlBar createButtonBar(MultiView mv) {
        ControlBar bar = new MainControlBar(getServiceContext());

        activityCycle = bar.addImageButton(R.drawable.go_down_inverse);
        bar.add(new MvNextButton(mv));

        trackerState = new TrackerStateButton(this.getServiceContext());
        
        bar.addView(trackerState);
        bar.setOnClickListener1(this);
        
        trackerState.setOnClickListener(trackerState);

        return bar;
    }




    @Override
    public void onClick(View v) {
        if (v == activityCycle) {
            ActivitySwitcher.cycle(this);

        }
    }

    
    private void createDispatcher() {
            OsmOverlay overlayList[] = {
                    new GpxOverlayListOverlay(map, getServiceContext()),
                    new GpxDynOverlay(map, getServiceContext(), GpxInformation.ID.INFO_ID_TRACKER), 
                    new CurrentLocationOverlay(map),
                    new GridDynOverlay(map, getServiceContext()),
                    new NavigationBarOverlay(map),
                    new InformationBarOverlay(map),
                    new EditorOverlay(map, getServiceContext(), GpxInformation.ID.INFO_ID_EDITOR_DRAFT, edit),
            };
            map.setOverlayList(overlayList);
            
            OnContentUpdatedInterface[] target = new OnContentUpdatedInterface[] {
                    multiView,trackerState,this
            };

            ContentSource[] source = new ContentSource[] {
                    new EditorSource(getServiceContext(), edit),
                    new TrackerSource(getServiceContext()),
                    new CurrentLocationSource(getServiceContext()),
                    new OverlaySource(getServiceContext()),
            };
            setDispatcher(new RootDispatcher(source, target));
    }
}
