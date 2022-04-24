package edu.neu.madcourse.metu.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

public class GifView extends View {
    Movie movie;
    InputStream inStream;
    long startTime;


    public GifView(Context context) {
        super(context);
        try {
            inStream = getResources().getAssets().open("bg_home.gif");
            movie = Movie.decodeStream(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        super.onDraw(canvas);
        long now = System.currentTimeMillis();
        if (startTime == 0) {
            startTime = now;
        }
        int relTime = (int) ((now - startTime) % movie.duration());
        movie.setTime(relTime);
        float scalefactorx = (float) this.getWidth() / (float) movie.width();
        float scalefactory = (float) this.getHeight() / (float) movie.height();
        canvas.scale(scalefactorx,scalefactory);
        movie.draw(canvas, 1, 1);
        this.invalidate();
    }
}
