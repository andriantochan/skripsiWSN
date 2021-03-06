package SensorNode;
import java.io.IOException;

import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.device.at86rf231.AT86RF231RadioDriver;
import com.virtenio.misc.PropertyHelper;
import com.virtenio.preon32.node.Node;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;
import com.virtenio.vm.Time;


// kelas buat menghandle antar sensor
public class SensorManager {
	
	public static final Accelerometer accel = new Accelerometer();
	public static boolean sensing = true;
	public Thread threadSense;
	public static float[] resTemp;
	
	//Setting address sensor
	public static int COMMON_PANID = PropertyHelper.getInt("radio.panid", 0xCAFF);
	public static int[] node_list = new int[] { 
			PropertyHelper.getInt("radio.panid", 0xABFE),
			PropertyHelper.getInt("radio.panid", 0xDAAA),
			PropertyHelper.getInt("radio.panid", 0xDAAB),
			PropertyHelper.getInt("radio.panid", 0xDAAC),
			PropertyHelper.getInt("radio.panid", 0xDAAD),
			PropertyHelper.getInt("radio.panid", 0xDAAE)
	};
	
	
	//Setting sensor
	
	private static final String sensorId = "SensorC";
	private static int PREVIOUS_NODE_ADDRESS = node_list[0];
	private static int SENSOR_NODE_ADDRESS = node_list[3];
	private static int NEXT_NODE_ADDRESS = 0;
	private static int BASE_STATION_ADRESS = node_list[0];
	
	
	public static void main(String[] args) throws Exception{
		accel.init();
		System.out.println(sensorId + "waiting...");
		starts();
	}
	
	public static void starts() {
		try {
			AT86RF231 trans = Node.getInstance().getTransceiver();
			trans.open();
			trans.setAddressFilter(COMMON_PANID, SENSOR_NODE_ADDRESS, SENSOR_NODE_ADDRESS, false);
			final RadioDriver radioDriver = new AT86RF231RadioDriver(trans);
			final FrameIO fio = new RadioDriverFrameIO(radioDriver);
			recieve(fio);
		}
		catch(Exception e) {
			
		}
	}
	
	public static void send(String message, int source, int destination, FrameIO fio) {
		int frameControl = Frame.TYPE_DATA | Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.ACK_REQUEST
							| Frame.SRC_ADDR_16;
		final Frame sentFrame = new Frame(frameControl);
		sentFrame.setDestPanId(COMMON_PANID);
		sentFrame.setDestAddr(destination);
		sentFrame.setSrcAddr(source);
		sentFrame.setPayload(message.getBytes());
		try {
			fio.transmit(sentFrame);
		}
		catch (Exception e) {
			
		}
	}
	
	public static void recieve(final FrameIO fio) {
		Thread thread = new Thread() {
			public void run() {
				Frame frame = new Frame();
				while(true) {
					try {
						fio.receive(frame);
						byte[] content = frame.getPayload();
						String str = new String(content, 0, content.length);
						System.out.println(str);
						if(str.charAt(0)!= '@'){
							send(str, SENSOR_NODE_ADDRESS, PREVIOUS_NODE_ADDRESS, fio);
						}
						if(str.substring(0, 2).equalsIgnoreCase("@1")) {
							long curTime = Time.currentTimeMillis();
							send("@1", SENSOR_NODE_ADDRESS, NEXT_NODE_ADDRESS, fio);
							send("1" + sensorId + " online "+ curTime+"\n", SENSOR_NODE_ADDRESS, PREVIOUS_NODE_ADDRESS, fio);

						}
						else if(str.substring(0, 2).equalsIgnoreCase("@2")) {
							System.out.println("Sensing start");
							send("@2", SENSOR_NODE_ADDRESS, NEXT_NODE_ADDRESS, fio);
							sensing = true;
							Thread sensingThread = new Thread() {
								public void run() {
									try {
										while(sensing) {
											resTemp = accel.sensing();
											String message = "2"+sensorId+", "+resTemp[0]+", "+resTemp[1]+", "+resTemp[2]+"\n";
											send(message, SENSOR_NODE_ADDRESS, PREVIOUS_NODE_ADDRESS, fio);	
											Thread.sleep(100);
										}
									}
									catch (InterruptedException e) {
										e.printStackTrace();
									} 
									catch (Exception e) {
										e.printStackTrace();
									}
								}
							};
							sensingThread.start();
							
						}
						else if(str.substring(0, 2).equalsIgnoreCase("@3")) {
							if(sensing) {
								String message = "3Sensing has stopped";
								send("@3", SENSOR_NODE_ADDRESS, NEXT_NODE_ADDRESS, fio);
								send(message, SENSOR_NODE_ADDRESS, PREVIOUS_NODE_ADDRESS, fio);
								sensing = false;
							}
						}
						else if(str.substring(0, 2).equalsIgnoreCase("@4")) {
							send("@4", SENSOR_NODE_ADDRESS, NEXT_NODE_ADDRESS, fio);
							send("4", SENSOR_NODE_ADDRESS, PREVIOUS_NODE_ADDRESS, fio);
							System.out.println("Exit from the program");
							System.exit(0);
						}
					}
					catch (IOException e) {
						
					}
				}
			}
		};
		thread.start();
	}
}