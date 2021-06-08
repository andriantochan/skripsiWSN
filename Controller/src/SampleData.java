import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SampleData {

    public String sensorId;
    public List<Double> X;
    public List<Double> Y;
    public List<Double> Z;
    public final int MAX;

    public SampleData(int banyak) {
        MAX = banyak;
        this.X = Collections.synchronizedList(new ArrayList<>());
        this.Y = Collections.synchronizedList(new ArrayList<>());
        this.Z = Collections.synchronizedList(new ArrayList<>());
    }

    public void addX(double titikX) {
        this.X.add(titikX);
        if (this.X.size() > MAX) {
            this.X.remove(0);
        }
    }

    public void addY(double titikY) {
        this.Y.add(titikY);
        if (this.Y.size() > MAX) {
            this.Y.remove(0);
        }
    }

    public void addZ(double titikZ) {
        this.Z.add(titikZ);
        if (this.Z.size() > MAX) {
            this.Z.remove(0);
        }
    }

    public Double[] getX() {
        return this.X.toArray(new Double[this.X.size()]);
    }

    public Double[] getY() {
        return this.Y.toArray(new Double[this.Y.size()]);
    }

    public Double[] getZ() {
        return this.Z.toArray(new Double[this.Z.size()]);
    }
}
