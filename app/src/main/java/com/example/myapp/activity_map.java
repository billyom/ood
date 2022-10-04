package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class activity_map extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    // TODO https://stackoverflow.com/questions/6178896/how-to-draw-a-line-in-imageview-on-android

    private Paint mPaintText;
    private Paint mPaintUndropped;
    private Paint mPaintDropped;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private ImageView mMapView;

    private LocationManager mLocMgr;
    private static final int PERMISSIONS_REQUEST = 0;
    public static final String TAG = "activity_map";


    int mColorWater;
    int mColorUndroppedMark;
    int mColorDroppedMark;
    int mColorMark;
    int mRadius;

    int mDropCnt = 0;
    boolean mDropCntUpdated = false;
    int mDroppedMarkIdx = 0;
    List<Mark> mMarks;
    List<Location> mDbgMarkLocs;
    private Location mCurrentLoc;
    private Location mTargetLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mMapView = findViewById(R.id.mapView);
        mColorWater = ResourcesCompat.getColor(getResources(), R.color.water, null);
        mColorUndroppedMark = ResourcesCompat.getColor(getResources(), R.color.undroppedMark, null);
        mColorDroppedMark = ResourcesCompat.getColor(getResources(), R.color.droppedMark, null);
        mColorMark = mColorDroppedMark;

        mPaintText = new Paint();
        mPaintText.setTextSize(24);

        mPaintUndropped = new Paint();
        mPaintUndropped.setStrokeWidth(6);
        mPaintUndropped.setStyle(Paint.Style.STROKE);
        mPaintUndropped.setPathEffect(new DashPathEffect(new float[] {20f,10f, 10f, 10f}, 0f));
        mPaintUndropped.setColor(mColorMark);

        mPaintDropped = new Paint(mPaintUndropped);
        mPaintDropped.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintDropped.setPathEffect(null);

        mMarks = new ArrayList<Mark>();
        mMarks.add(new Mark(0, "Leeward", 2f/3f, 6f/10f));
        mMarks.add(new Mark(1, "Windward", 2f/3f, 1f/10f));
        mMarks.add(new Mark(2, "Gybe", 1f/5f, 3.5f/10f));

        mDbgMarkLocs = new ArrayList<Location>();
        mDbgMarkLocs.add(new Location(LocationManager.GPS_PROVIDER));
        mDbgMarkLocs.add(new Location(LocationManager.GPS_PROVIDER));
        mDbgMarkLocs.add(new Location(LocationManager.GPS_PROVIDER));
        Location loc = mDbgMarkLocs.get(0);
        loc.setLatitude(52.818569);
        loc.setLongitude(-8.762453);
        loc = mDbgMarkLocs.get(1);
        loc.setLatitude(52.824690);
        loc.setLongitude(-8.762453);
        loc = mDbgMarkLocs.get(2);
        loc.setLatitude(52.822231);
        loc.setLongitude(-8.767305);

        mMapView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mCurentLoc == null)
                {
                    // probably location service is off / location unavailable
                    return false;
                }

                // Go through each of the marks and update the relevant one if any
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int markIdx = 0;
                    for (Mark m : mMarks) {
                        if (Math.sqrt(Math.pow(event.getX() - m.mScreenX, 2) + Math.pow(event.getY() - m.mScreenY, 2)) < mRadius) {
                            m.isDropped = !m.isDropped;   // toggle dropped/undropped state
                            if (m.isDropped) {
                                m.mLoc = mCurrentLoc;
                                mDropCnt++;
                                mDroppedMarkIdx = markIdx;
                                Log.i(TAG, String.format("Dropped %s at %s", m.mName, m.mLoc));
                            } else {
                                mDropCnt--;
                                mDroppedMarkIdx = -1;
                                Log.i(TAG, String.format("Picked up %s", m.mName));
                            }
                            mDropCntUpdated = true;
                            break;  // There can be only one matching mark
                        }
                        markIdx++;
                    }
                    updateMap();
                }
                // TODO false => event not consumed, true consumed. Probably should be true
                return false;
            }
        });

        reqLocUpdates();
    }

    private void updateMap() {
        int vWidth = mMapView.getWidth();
        int vHeight = mMapView.getHeight();
        mRadius = vHeight / 40;

        mBitmap = Bitmap.createBitmap(vWidth, vHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mMapView.setImageBitmap(mBitmap);

        //getResources();  // TODO remove ?

        mCanvas.drawColor(mColorWater);

        int boatScreenX = 0;
        int boatScreenY = 0;

        // Draw each mark on the map
        for (Mark m : mMarks) {

            m.calcXY(vWidth, vHeight);
            boatScreenX += m.mScreenX;
            boatScreenY += m.mScreenY;

            if (m.isDropped) {
                mCanvas.drawCircle(m.mScreenX, m.mScreenY, mRadius, mPaintDropped);
            }
            else {
                mCanvas.drawCircle(m.mScreenX, m.mScreenY, mRadius, mPaintUndropped);
            }
        }

        if (mDropCnt == 2 && mTargetLoc == null) {
            // Two or more marks are laid and target loc has not been calculated yet.
            // Calculate and instruct where to lay the remaining marks.

            // Draw the boat in the 'centre-of-gravity' of the marks
            boatScreenX /= mMarks.size();
            boatScreenY /= mMarks.size();
            Drawable boatDrawable = ContextCompat.getDrawable(activity_map.this, R.drawable.ic_boat);
            boatDrawable.setBounds(boatScreenX - mRadius, boatScreenY - mRadius, boatScreenX + mRadius, boatScreenY + mRadius);
            boatDrawable.draw(mCanvas);

            // Locate two dropped and one un-dropped mark and display navigation hint
            Mark refMark1 = null;
            Mark refMark2 = null;
            Mark targetMark = null;

            for (Mark m : mMarks) {
                if (m.isDropped) {
                    if (refMark1 == null) {
                        refMark1 = m;
                        Log.i(TAG, String.format("refMark1 is %s", refMark1.mName));
                    } else if (refMark2 == null) {
                        refMark2 = m;
                        Log.i(TAG, String.format("refMark2 is %s", refMark2.mName));
                    }
                } else if (targetMark == null) {
                    // choose first un-dropped mark as the target mark
                    targetMark = m;
                    Log.i(TAG, String.format("Target mark is %s", targetMark.mName));
                }
            }

            // Actual distance and bearing from one dropped mark to the other
            float bearingDeg = refMark1.mLoc.bearingTo(refMark2.mLoc);
            float distanceM = refMark1.mLoc.distanceTo(refMark2.mLoc);

            Log.i(TAG, String.format("%s mark %.0f deg and %.0f m from %s mark",
                    refMark2.mName, bearingDeg, distanceM, refMark1.mName));

            // Chart distance and bearing from one dropped mark to the other
            float chartBearingDeg = refMark1.chartBearingTo(refMark2);
            float chartDistance = refMark1.distanceTo(refMark2);

            float chartToRealBearingAdj = bearingDeg - chartBearingDeg;
            float chartToRealScaleMult = distanceM / chartDistance;

            // On the chart we are at the mark that was just dropped
            // Get chart distance & bearing to target (un-dropped) mark
            Mark chartPos = mMarks.get(mDroppedMarkIdx);
            float chartTargetBearingDeg = chartPos.chartBearingTo(targetMark);
            float chartTargetDistance = chartPos.distanceTo(targetMark);

            // Convert chart bearing & distance to true bearing & distance
            float trueTargetBearingDeg = chartTargetBearingDeg + chartToRealBearingAdj;
            float trueTargetDistanceM = chartTargetDistance * chartToRealScaleMult;
            mTargetLoc = adjustLocation(chartPos.mLoc, trueTargetBearingDeg, trueTargetDistanceM);

            String msg = String.format("targetLoc is %f degrees %f metres", trueTargetBearingDeg, trueTargetDistanceM);
            Log.i(TAG, msg);
        }

        if (mDropCnt == 2 && mTargetLoc != null) {
            Log.i(TAG, String.format("targetLoc is %.0f degrees %.0f metres", mCurrentLoc.bearingTo(mTargetLoc), mCurrentLoc.distanceTo(mTargetLoc));
        }

        if (mDropCnt == 1) {
            mTargetLoc = null;
        }

        mMapView.invalidate();
    }

    /* Return a new location based on another location plus a given bearing and distance */
    private Location adjustLocation(Location loc, double bearingDegs, double distanceM){

        double R = 637810;   // Radius of the Earth

        double initialLat = Math.toRadians(loc.getLatitude());
        double initialLon = Math.toRadians(loc.getLongitude());

        double destLat = Math.asin( Math.sin(initialLat)*Math.cos(distanceM/R) +
               Math.cos(initialLat)*Math.sin(distanceM/R)*Math.cos(bearingDegs));

        double destLon = initialLon + Math.atan2(Math.sin(bearingDegs)*Math.sin(distanceM/R)*Math.cos(initialLat),
                Math.cos(distanceM/R)-Math.sin(initialLat)*Math.sin(destLat));

        destLat = Math.toDegrees(destLat);
        destLon = Math.toDegrees(destLon);

        Location destLoc = new Location(loc);
        loc.setLatitude(destLat);
        loc.setLongitude(destLon);

        return destLoc;
    }

    // Registers callback for location updates
    public void reqLocUpdates() {
        if (ContextCompat.checkSelfPermission(activity_map.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity_map.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity_map.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST);
            return;
        }

        mLocMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 10L, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                mCurrentLoc = location;
                updateMap();
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            // Check if the only required permission has been granted
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Location permission has been granted
                Log.i(TAG, String.valueOf(R.string.loc_perm_granted));
                Snackbar.make(mMapView, R.string.loc_perm_granted,
                        Snackbar.LENGTH_SHORT).show();
                reqLocUpdates();
            } else {
                Log.i(TAG, String.valueOf(R.string.loc_perm_not_granted));
                Snackbar.make(mMapView, R.string.loc_perm_not_granted,
                        Snackbar.LENGTH_SHORT).show();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onMapClick(View view) {
        return;

        /*
        int vWidth = view.getWidth();
        int vHeight = view.getHeight();

        Random r = new Random();
        int txtX = r.nextInt((int) (vHeight * 0.9));
        int txtY = r.nextInt((int) (vWidth * 0.5));

        mBitmap = Bitmap.createBitmap(vWidth, vHeight, Bitmap.Config.ARGB_8888);
        mMapView.setImageBitmap(mBitmap);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawText("Hello Wurld!", txtX, txtY, mPaintText);
        view.invalidate();
*/
    }

    public void onGotoBoatsClick(View view) {
        finish();
    }

}


