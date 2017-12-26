package com.example.android.mygarden;

import android.app.IntentService;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;

/**
 * Created by Smktelkom on 8/11/2017.
 */

public class PlantWateringService extends IntentService {

    public static final String ACTION_WATER_PLANTS = "com.example.android.mygarden.action.water_plants";
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     //* @param name Used to name the worker thread, important only for debugging.
     */
    public static final String ACTION_UPDATE_PLANT_WIDGETS = "com.example.android.mygarden.action.update_plant_widgets";

    public PlantWateringService() {
        super("PlantWaterngService");
    }


    public static void startActionWaterPlants(Context context){
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
        context.startService(intent);
    }

    public static void startActionUpdatePlantWidgets(Context context) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_UPDATE_PLANT_WIDGETS);
        context.startService(intent);}

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null){
            final String action = intent.getAction();
            if (ACTION_WATER_PLANTS.equals(action)){
                handleActionWatelPlants();
            } else if (ACTION_UPDATE_PLANT_WIDGETS.equals(action)){
                handleActionWatelPlants();
            }

        }
    }

    private void handleActionWatelPlants() {
        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);

        getContentResolver().update(PLANT_URI,contentValues,PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME + ">?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});
    }

    private void handleActionUpdatePlantWidgets() {
                //Query to get the plant that's most in need for water (last watered)
                        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
                Cursor cursor = getContentResolver().query(
                                PLANT_URI,
                                null,
                                null,
                                null,
                                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME
                                );
                // Extract the plant details
                int imgRes = R.drawable.grass; // Default image in case our garden is empty
                if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int createTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
                        int waterTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
                       int plantTypeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
                        long timeNow = System.currentTimeMillis();
                        long wateredAt = cursor.getLong(waterTimeIndex);
                        long createdAt = cursor.getLong(createTimeIndex);
                        int plantType = cursor.getInt(plantTypeIndex);
                        cursor.close();
                        imgRes = PlantUtils.getPlantImageRes(this, timeNow - createdAt, timeNow - wateredAt, plantType);
                    }
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));
                //Now update all widgets
                        PlantWidgetProvider.updatePlantWidgets(this, appWidgetManager, imgRes, appWidgetIds);
            }
}
