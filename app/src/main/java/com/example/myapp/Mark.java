package com.example.myapp;

import android.location.Location;

public class Mark {

    public Location mLoc = null;      // prev. or current location when dropped
    public boolean isDropped = false;

    public int mCourseOrder;    // 0 => 1st mark on course, 1 => 2nd etc
    public float mDiagramX;     // Ideal X location on the course from 0 to 1.0
    public float mDiagramY;     // Ideal Y location on the course from 0 to 1.0
    public int mScreenX;        // Screen co-ord corresponding to mDiagramX
    public int mScreenY;        // Screen co-ord corresponding to mDiagramY
    public String mName;

    public Mark(int courseOrder, String name, float diagramX, float diagramY){
        mCourseOrder = courseOrder;
        mDiagramX = diagramX;
        mDiagramY = diagramY;

        mScreenX = 0;
        mScreenY = 0;

        mName = name;
    }

    // Populate the screen co-ords for a Mark
    // TODO need to take into account the differing aspect ratios of the course an the screen
    // and where in the screen the course is to be displayed.
    void calcXY(int vWidth, int vHeight) {
        mScreenX = (int) (vWidth * mDiagramX);
        mScreenY = (int) (vHeight * mDiagramY);
    }

    // Distance is unitless but will not exceed sqrt(2) given that diagramX/Y is in range 0.0 - 1.0
    float distanceTo(Mark that) {
        return (float) Math.sqrt(
                Math.pow(mDiagramX - that.mDiagramX, 2) +
                Math.pow(mDiagramY - that.mDiagramY, 2)
        );
    }

    // Returns bearing in degrees given that 'up' on the diagram is 0 degrees.
    float chartBearingTo(Mark that) {

        // Get sin of angle between x-axis running through this mark and 'that' mark.
        // Sin = Opposite/Hypotenuse
        // NOTE: DiagramY *increases* from top of chart to bottom
        float angleSine = (mDiagramY - that.mDiagramY) /     // opposite
                            distanceTo(that);                // hypotenuse

        // Bearing :) in mind that Sin(x) == Sin(180 - x)
        double rads = Math.asin(angleSine);
        double angle = Math.toDegrees(rads); // angle will be range -90 to +90 degs

        if (mDiagramX < that.mDiagramX) {
            // that Mark is to left of this Mark => 180 < bearing < 360.
            angle = 90 - angle;
        } else {
            // that Mark is to right of this Mark => 0 >= bearing <= 180
            angle = 270 + angle;
        }
        angle = (int) angle % 360;   // Normalise bearing in 0 - 360 range. Also rounds to nearest deg.
        return  (float) angle;
    }
}
