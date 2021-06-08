public class DataFrequency {

    public String sensorId;
    public Double X;
    public Double Y;
    public Double Z;

    public DataFrequency() {
        this.X = 0.0;
        this.Y = 0.0;
        this.Z = 0.0;
    }

    public Double getX() {
        return this.X;
    }

    public Double getY() {
        return this.Y;
    }

    public Double getZ() {
        return this.Z;
    }

    public void setX(double X) {
        this.X = X;
    }

    public void setY(double Y) {
        this.Y = Y;
    }

    public void setZ(double Z) {
        this.Z = Z;
    }
}
