package ch.bailu.aat.services.directory;

import android.database.Cursor;

import java.io.File;

import ch.bailu.aat.gpx.GpxInformation;
import ch.bailu.aat.preferences.SolidDirectoryQuery;
import ch.bailu.aat.services.ServiceContext;

public class IteratorSummary extends IteratorAbstract {

    private GpxInformation info = GpxInformation.NULL;
    private final File directory;

    public IteratorSummary(ServiceContext sc) {
        super(sc);
        directory = new File(new SolidDirectoryQuery(sc.getContext()).getValueAsString());
        query();
    }

    @Override
    public GpxInformation getInfo() {
        return info;
    }

    @Override
    public void onCursorChanged(Cursor cursor, String fid) {
        //if (cursor.getCount()>0) {
            info = new GpxInformationDbSummary(directory, cursor);
        //} else {
        //    info = GpxInformation.NULL;
        //}
    }

}
