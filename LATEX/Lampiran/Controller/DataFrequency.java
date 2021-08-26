import java.io.IOException;

public class DataFrequency {

    public String sensorId;
    public Double X;
    public Double Y;
    public Double Z;
    private int counter = 0;
    private RenderChart rc;
    
    public DataFrequency() {
        this.X = 0.0;
        this.Y = 0.0;
        this.Z = 0.0;
    }
    
    public void setRender(RenderChart rc) {
    	this.rc =rc;
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
    public void setZ(double Z) throws IOException {
        this.Z = Z;
        counter++;
        if(counter == RenderChart.MAX_COUNTER) {
        	counter = 0;
        	rc.render();
        }
    }
}
