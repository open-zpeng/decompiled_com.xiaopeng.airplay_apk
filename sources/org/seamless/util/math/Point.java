package org.seamless.util.math;
/* loaded from: classes.dex */
public class Point {
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public Point multiply(double by) {
        return new Point(this.x != 0 ? (int) (this.x * by) : 0, this.y != 0 ? (int) (this.y * by) : 0);
    }

    public Point divide(double by) {
        return new Point(this.x != 0 ? (int) (this.x / by) : 0, this.y != 0 ? (int) (this.y / by) : 0);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Point point = (Point) o;
        if (this.x == point.x && this.y == point.y) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.x;
        return (31 * result) + this.y;
    }

    public String toString() {
        return "Point(" + this.x + "/" + this.y + ")";
    }
}
