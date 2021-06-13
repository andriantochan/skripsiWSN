package SensorNode;
import com.virtenio.driver.device.ADXL345;
import com.virtenio.driver.gpio.GPIO;
import com.virtenio.driver.gpio.NativeGPIO;
import com.virtenio.driver.spi.NativeSPI;


public class Accelerometer {
	private ADXL345 sensorAccel;
	private GPIO accelCs;
	
	public void init() throws Exception{
		accelCs = NativeGPIO.getInstance(20);
		NativeSPI spi = NativeSPI.getInstance(0);
		
		spi.open(ADXL345.SPI_MODE, ADXL345.SPI_BIT_ORDER, ADXL345.SPI_MAX_SPEED);
		
		sensorAccel = new ADXL345(spi, accelCs);
		
		sensorAccel.open();
		sensorAccel.setDataFormat(ADXL345.DATA_FORMAT_RANGE_2G);
		sensorAccel.setDataRate(ADXL345.DATA_RATE_3200HZ);
		sensorAccel.setPowerControl(ADXL345.POWER_CONTROL_MEASURE);
		
	}
	
	public float[] sensing() throws Exception{
		short[] resTemp = new short[3];
		float[] res = new float[3];
		sensorAccel.getValuesRaw(resTemp, 0);
		sensorAccel.convertRaw(resTemp, 0, res, 0);
//		for (int i =0; i<3; i++) {
//			res[i] *= sensorAccel.getConversionScale();
//			
//		}
		return res;
	}
}
