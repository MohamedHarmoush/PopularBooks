package com.example.harmoush.popularbooks;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Harmoush on 2/3/2018.
 */

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataProvider(this,intent);
    }
}
