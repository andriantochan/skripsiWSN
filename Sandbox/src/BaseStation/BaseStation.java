package BaseStation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.device.at86rf231.AT86RF231RadioDriver;
import com.virtenio.driver.usart.NativeUSART;
import com.virtenio.driver.usart.USART;
import com.virtenio.driver.usart.USARTException;
import com.virtenio.driver.usart.USARTParams;
import com.virtenio.misc.PropertyHelper;
import com.virtenio.preon32.examples.common.USARTConstants;
import com.virtenio.preon32.node.Node;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;
import com.virtenio.vm.Time;

public class BaseStation {
	private static USART usart; 
	private static OutputStream output;
	
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
		
	private static int curr = node_list[0];
	private static int[] connectedNode = new int[] {
		node_list[1],
		node_list[2],
		node_list[3],
		node_list[4],
		node_list[5]
	};
	
	public static HashMap<String, Integer> addressNodeMap;
	
	private static boolean done;
	private static String ackAddress;
	private static String result;
	
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
			Thread.sleep(100);
		}
		catch (Exception e) {

		}
	}
	
	public static void main(String[] args) throws USARTException {
		addressNodeMap = new HashMap<String, Integer>();
		for (int i=1; i<connectedNode.length; i++) {
			addressNodeMap.put("Sensor"+i, connectedNode[i-1]);
		}
		
		usart = configUSART();
		output = usart.getOutputStream();
		
		ackAddress = "";
		result = "";
		done = false;
		
		Thread thread = new Thread() {
			public void run() {
				starts();
			}
		};
		thread.start();
	}
	
	public static void starts() {
		try {
			AT86RF231 t = Node.getInstance().getTransceiver();
			t.open();
			t.setAddressFilter(COMMON_PANID, curr, curr, false);
			final RadioDriver radioDriver = new AT86RF231RadioDriver(t);
			final FrameIO fio = new RadioDriverFrameIO(radioDriver);
			
			
			Thread thread1 = new Thread() {
				public void run() {
					while(true) {
						String tempRes;
						int input;
						try {
							input = usart.read();
							if(input == 1 ) {
								long curTime = Time.currentTimeMillis();
								tempRes = new String("BaseStation online "+ curTime);
								output.write(tempRes.getBytes());
								System.out.println();
								usart.flush();
								for (int i=0; i<connectedNode.length; i++) {
									send("@1", curr, connectedNode[i], fio);
								}
							}
							else if(input == 2) {
								for (int i=0; i<connectedNode.length; i++) {
									send("@2", curr, connectedNode[i], fio);
								}
							}
							else if(input == 3) {
								for (int i=0; i<connectedNode.length; i++) {
									send("@3", curr, connectedNode[i], fio);
								}
								usart = configUSART();
								output = usart.getOutputStream();
								done = false;
								ackAddress = "";
								result = "";
							}
							else if(input == 4) {
								for (int i=0; i<connectedNode.length; i++) {
									send("@4", curr, connectedNode[i], fio);
								}
							}
							Thread.sleep(100);
						}
						catch(USARTException e) {
							
						}
						catch(InterruptedException e) {
							
						} 
						catch (IOException e) {
							
						}
					}
				}
			};
			thread1.start();
			
			Thread thread2 = new Thread() {
				public void run() {
					recieve(fio);
				}
			};
			thread2.start();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void recieve(final FrameIO fio){
		while(true) {
			Frame frame = new Frame();
			try {
				fio.receive(frame);
				byte[] content = frame.getPayload();
				String str = new String(content, 0, content.length);
				if(str.trim().charAt(0)=='1') {
					usart.write(str.getBytes());
					usart.flush();
				}
				else if(str.trim().charAt(0)=='2') {
					usart.write(str.getBytes());
					usart.flush();
				}
				else if(str.trim().charAt(0)=='3') {
					usart.write(str.getBytes());
					usart.flush();
				}
				else if(str.trim().charAt(0)=='4') {
					usart.write(str.getBytes());
					usart.flush();
				}
			}
			catch(USARTException e) {
				
			}
			catch(IOException e) {
				
			}
		}
	}
	
	
	
	public static USART configUSART() {
		USARTParams params = USARTConstants.PARAMS_115200;
		NativeUSART usart = NativeUSART.getInstance(0);
		try {
			usart.close();
			usart.open(params);
			return usart;
		}
		catch(Exception e) {
			return null;
		}
	}
}
