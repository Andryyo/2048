package com.Andryyo._2048;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Random;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Андрей
 * Date: 26.03.14
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
public class C2048View extends LinearLayout implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private final int fieldSize = 5;
    private final int baseElement = 2;

    private int width;
    private int height;
    private int elementSize;
    private Paint paint = new Paint();

    public static enum SLIDE {SLIDE_UP, SLIDE_RIGHT, SLIDE_DOWN, SLIDE_LEFT};
    public static enum TRANSFORM {ROTATE_LEFT, ROTATE_RIGHT, FLIP};

    private GestureDetector gestureDetector;

    private int[][] field = new int[fieldSize][fieldSize];

    public C2048View(Context context)
    {
        super(context);
        setWillNotDraw(false);
        for (int i = 0; i<fieldSize; i++)
            for (int j = 0; j<fieldSize; j++)
                field[i][j] = 0;
        this.setOnTouchListener(this);
        addNext();
        gestureDetector = new GestureDetector(this);
    }

    //public C2048View(Context context, AttributeSet attrs) {
    //    super(context, attrs);
    //}

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        width = w;
        height = h;
        elementSize = width < height ? width/fieldSize : height/fieldSize;
    }

    @Override
    public boolean onTouch(View v, MotionEvent me)
    {
        gestureDetector.onTouchEvent(me);
        return true;
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i<fieldSize; i++)
            for (int j = 0; j<fieldSize; j++)
                canvas.drawRect(j*elementSize+2, i*elementSize+2, (j+1)*elementSize - 2, (i+1)*elementSize - 2, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(elementSize/2);
        for (int i = 0; i<fieldSize; i++)
            for (int j = 0; j<fieldSize; j++)
                if (field[i][j]!=0)
                    canvas.drawText(String.valueOf(field[i][j]), j*elementSize + elementSize*1/5, i*elementSize + elementSize*4/5, paint);
    }

    private void slide(SLIDE slide)
    {
        int[][] bufField = null;
        switch (slide)
        {
            case SLIDE_DOWN:
                bufField = transform(field, TRANSFORM.ROTATE_RIGHT);
                break;
            case SLIDE_LEFT:
                bufField = field;
                break;
            case SLIDE_RIGHT:
                bufField = transform(field, TRANSFORM.FLIP);
                break;
            case SLIDE_UP:
                bufField = transform(field, TRANSFORM.ROTATE_LEFT);
                break;
        }

        int col;
        for (int row = 0; row<fieldSize; row++)
        {
            col = 0;
            while (col<fieldSize-1)
            {
                int k;
                for (k = col; k<fieldSize; k++)
                    if (bufField[row][k]!=0)
                        break;
                if (k != fieldSize && k != col)
                {
                    for (int i = 0; i<fieldSize-k;i++)
                        bufField[row][col+i] = bufField[row][i+k];
                    for (int i = fieldSize-k+col; i<fieldSize; i++)
                        bufField[row][i] = 0;
                    if (col>0)
                        col--;
                }
                if (matchElements(bufField[row][col], bufField[row][col+1]))
                {
                    bufField[row][col] = appendElements(bufField[row][col], bufField[row][col+1]);
                    for (int i = col+1; i<fieldSize-1; i++)
                        bufField[row][i] = bufField[row][i+1];
                    bufField[row][fieldSize-1] = 0;
                    col=-1;
                }
                col++;
            }
        }
        switch (slide)
        {
            case SLIDE_DOWN:
                field = transform(bufField, TRANSFORM.ROTATE_LEFT);
                break;
            case SLIDE_LEFT:
                field = bufField;
                break;
            case SLIDE_RIGHT:
                field = transform(bufField, TRANSFORM.FLIP);
                break;
            case SLIDE_UP:
                field = transform(bufField, TRANSFORM.ROTATE_RIGHT);
                break;
        }
        addNext();
        invalidate();
    }

    private int[][] transform(int[][] field, TRANSFORM transform)
    {
        int [][] bufField = new int[fieldSize][fieldSize];
        switch (transform)
        {
            case ROTATE_LEFT:
                for (int i = 0; i<fieldSize; i++)
                    for (int j = 0; j<fieldSize; j++)
                        bufField[fieldSize - j - 1][i] = field[i][j];
                break;
            case ROTATE_RIGHT:
                for (int i = 0; i<fieldSize; i++)
                    for (int j = 0; j<fieldSize; j++)
                        bufField[j][fieldSize - i - 1] = field[i][j];
                break;
            case FLIP:
                bufField = transform(field, TRANSFORM.ROTATE_RIGHT);
                bufField = transform(bufField, TRANSFORM.ROTATE_RIGHT);
                break;
        }
        return bufField;
    }

    private class CPoint
    {
        CPoint(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
        int x,y;
    }

    private boolean addNext()
    {
        Vector<CPoint> points = new Vector<CPoint>();
        for (int i = 0; i<fieldSize; i++)
            for (int j = 0; j<fieldSize; j++)
                if (field[i][j] == 0)
                {
                    points.add(new CPoint(j,i));
                }
        if (points.isEmpty())
            return false;
        Random random = new Random();
        int r = Math.abs(random.nextInt() % points.size());
        field[points.elementAt(r).y][points.elementAt(r).x] = baseElement;
        return true;
    }

    private boolean matchElements(int e1, int e2)
    {
        if (e1 == 0)
            return false;
        return  (e1 == e2);
    }

    private int appendElements(int e1, int e2)
    {
        return e1 + e2;
    }


    @Override
    public boolean onDown(MotionEvent event) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent event, MotionEvent event2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
    }

    @Override
    public boolean onFling(MotionEvent event, MotionEvent event2, float v, float v2) {
        int diffX = (int)(event2.getX() - event.getX());
        int diffY = (int)(event2.getY() - event.getY());
        if (Math.abs(diffX)>=Math.abs(diffY))
        {
            if (diffX>=0)
                slide(SLIDE.SLIDE_RIGHT);
            else
                slide(SLIDE.SLIDE_LEFT);
        }
        else
        {
            if (diffY>=0)
                slide(SLIDE.SLIDE_DOWN);
            else
                slide(SLIDE.SLIDE_UP);
        }
        return true;
    }

}
