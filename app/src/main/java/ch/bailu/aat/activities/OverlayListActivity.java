package ch.bailu.aat.activities;

import android.content.Intent;

import java.io.File;

import ch.bailu.aat.R;
import ch.bailu.aat.description.ContentDescription;
import ch.bailu.aat.description.DateDescription;
import ch.bailu.aat.description.DistanceDescription;
import ch.bailu.aat.description.NameDescription;
import ch.bailu.aat.description.PathDescription;
import ch.bailu.aat.description.TrackSizeDescription;
import ch.bailu.aat.helpers.AppDirectory;



public class OverlayListActivity extends AbsGpxListActivity {
    @Override
    public ContentDescription[] getGpxListItemData() {
        return new ContentDescription[] {
                new DateDescription(this),
                new DistanceDescription(this),
                new NameDescription(this)
        };    		
    }

    @Override
    public ContentDescription[] getSummaryData() {
        return new ContentDescription[] {
                new NameDescription(this),
                new PathDescription(this),
                new TrackSizeDescription(this),
        };
    }
    
    
    @Override
    public void displayFile() {
        Intent intent=new Intent(this,GpxEditorActivity.class);
        startActivity(intent);
    }


    @Override
    public File getDirectory() {
        return AppDirectory.getDataDirectory(this, AppDirectory.DIR_OVERLAY); 
    }

    @Override
    public String getLabel() {
        return getString(R.string.intro_overlay_list);
    }

}
