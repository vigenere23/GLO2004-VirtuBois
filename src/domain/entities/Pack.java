package domain.entities;

import helpers.ConfigHelper;
import javafx.geometry.Point2D;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

public class Pack extends Drawable
{
    private double height;
    private LocalDate date;
    private LocalTime time;
    private String essence;
    private String plankSize;
    private int barcode;
    // private static final AtomicInteger count = new AtomicInteger(000000000000);

    public Pack(Point2D position) {
        super(position);
        setWidth(ConfigHelper.packWidth);
        setLength(ConfigHelper.packLength);
        setHeight(ConfigHelper.packHeight);
        setAngle(ConfigHelper.packAngle);
        setEssence(ConfigHelper.essence);
        setPlanckSize(ConfigHelper.plankSize);
        initDate();
        initTime();
        initBarcode();
    }

    public double getHeight()
    {
        return height;
    }

    public void setHeight(double height)
    {
        this.height = height;
    }

    public String getEssence()
    {
        return essence;
    }

    public void setEssence(String essence)
    {
        this.essence = essence;
    }

    public String getPlankSize() { return plankSize; }

    public void setPlanckSize(String plankSize) { this.plankSize = plankSize; };

    public LocalTime getTime()
    {
        return time;
    }

    public void setTime(LocalTime time) { this.time = time; }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    public int getBarcode() { return barcode; }

    private void initDate() {}

    private void initTime() {}

    private void initBarcode() {}
}
