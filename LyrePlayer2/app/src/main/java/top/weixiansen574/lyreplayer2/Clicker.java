package top.weixiansen574.lyreplayer2;

import android.os.RemoteException;
import android.os.SystemClock;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.TooManyListenersException;

public abstract class Clicker {
    protected IShizukuInputService service;
    Coordinate[] coordinates = new Coordinate[10];
    public Clicker(IShizukuInputService service) {
        this.service = service;
    }

    public abstract void down(int x,int y) throws RemoteException ;

    public abstract void up(int x,int y) throws RemoteException;

    public int putCoordinate(int x, int y) {
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate coordinate = coordinates[i];
            if (coordinate == null) {
                coordinates[i] = new Coordinate(x, y);
                return i;
            }
        }
        throw new TooManyTouchesException("Too many touches");
    }

    public int removeCoordinate(int x, int y) {
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate coordinate = coordinates[i];
            if (coordinate != null && coordinate.x == x && coordinate.y == y) {
                coordinates[i] = null;
                return i;
            }
        }
        throw new TouchNotFoundException("Touch not found");
    }

    public int getCoordinateSize(){
        int size = 0;
        for (Coordinate coordinate : coordinates) {
            if (coordinate != null) {
                size++;
            }
        }
        return size;
    }


    public static class Coordinate{
        public final int x;
        public final int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @NonNull
        @Override
        public String toString() {
            return "Coordinate{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Coordinate)) return false;
            Coordinate that = (Coordinate) o;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public static class TooManyTouchesException extends RuntimeException{
        public TooManyTouchesException(String tooManyTouches) {
            super(tooManyTouches);
        }
    }

    public static class TouchNotFoundException extends RuntimeException{
        public TouchNotFoundException(String touchNotFound) {
            super(touchNotFound);
        }
    }
}
