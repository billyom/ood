package com.example.myapp;

import java.util.ArrayList;
import java.util.List;

public class Course {
    List<Mark> mMarks = new ArrayList<Mark>();

    enum CourseType {OLYMPIC_TRIANGLE};

    public Course()
    {
        mMarks.add(new Mark(0, "",1.0f, 1.0f));
        mMarks.add(new Mark(1, "", 1.0f, 0.0f));
        mMarks.add(new Mark(2, "", 0f, 0.5f));
    }
}
