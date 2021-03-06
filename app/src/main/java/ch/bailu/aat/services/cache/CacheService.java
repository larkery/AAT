package ch.bailu.aat.services.cache;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.Closeable;

import ch.bailu.aat.helpers.AppBroadcaster;
import ch.bailu.aat.helpers.AppLog;
import ch.bailu.aat.services.ServiceContext;
import ch.bailu.aat.services.VirtualService;
import ch.bailu.aat.services.cache.ObjectHandle.Factory;



public class CacheService extends VirtualService {

    private final Self self;

    public Self getSelf() {
        return self;

    }

    
    public CacheService(ServiceContext sc) {
        super(sc);
        self = new SelfOn();
    }




    @Override
    public void appendStatusText(StringBuilder builder) {
        self.appendStatusText(builder);
    }


    
    @Override
    public void close() {
        self.close();
    }
    
    
    public static class Self implements Closeable {
        @Override
        public void close() {}

        public void addToBroadcaster(ObjectBroadcastReceiver b) {}
        public void onLowMemory() {}

        public ObjectHandle getObject(String id) {
            AppLog.d(this, id);
            return ObjectHandle.NULL;
        }

        public ObjectHandle getObject(String id, Factory factory) {
            AppLog.d(this, id);
            return ObjectHandle.NULL;
        }

        public void appendStatusText(StringBuilder builder) {}
    }


    public class SelfOn extends Self {
        public final ObjectTable table=new ObjectTable();
        public final ObjectBroadcaster broadcaster;

        public final ServiceContext scontext;

        public SelfOn() {
            
            scontext = getSContext();
            broadcaster = new ObjectBroadcaster(getSContext());


            AppBroadcaster.register(getContext(), onFileProcessed, AppBroadcaster.FILE_CHANGED_INCACHE);
        }

        @Override
        public void onLowMemory() {
            table.onLowMemory(this);
        }

        @Override
        public ObjectHandle getObject(String id, ObjectHandle.Factory factory) {
            
            return table.getHandle(id, factory, this);
        }

        @Override
        public ObjectHandle getObject(String id) {
            return table.getHandle(id, getSContext());
        }

        @Override
        public void appendStatusText(StringBuilder builder) {
            super.appendStatusText(builder);
            table.appendStatusText(builder);
        }


        @Override
        public void close() {
            table.logLocked();
            getContext().unregisterReceiver(onFileProcessed);
            broadcaster.close();
        }


        @Override
        public void addToBroadcaster(ObjectBroadcastReceiver b) {
            broadcaster.put(b);
        }
        
        private final BroadcastReceiver onFileProcessed = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                table.onObjectChanged(intent, SelfOn.this);
            }
        };
    }
}
