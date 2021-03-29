
public class SampleData {
	public String sensorId;
	public double[] X;
	public double[] Y;
	public double[] Z;
	
	public SampleData(int banyak) {
		this.X = new double[banyak];
		this.Y = new double[banyak];
		this.Z = new double[banyak];
	}
	
	public void setX(double titikX, int idx) {
		for (int i =0; i<this.X.length; i++) {
			this.X[idx] = titikX;
		}
	}
	
	public void setY(double titikY, int idx) {
		for (int i =0; i<this.Y.length; i++) {
			this.Y[idx] = titikY;
		}
	}
	
	public void setZ(double titikZ, int idx) {
		for (int i =0; i<this.Z.length; i++) {
			this.Z[idx] = titikZ;
		}
	}
	
	
	public double[] getX() {
		return this.X;
	}
	
	public double[] getY() {
		return this.Y;
	}
	
	public double[] getZ() {
		return this.Z;
	}
	
	
}
