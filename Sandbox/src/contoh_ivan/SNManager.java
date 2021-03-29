package contoh_ivan;

import java.io.IOException;
import java.util.Random;

import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.device.at86rf231.AT86RF231RadioDriver;
import com.virtenio.misc.PropertyHelper;
import com.virtenio.preon32.node.Node;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;
import com.virtenio.vm.Time;

public class SNManager {

	// Setting Address
	private static int COMMON_PANID = PropertyHelper.getInt("radio.panid", 0xCAFF);
	private static int[] node_list = new int[] { PropertyHelper.getInt("radio.panid", 0xABFE),
			PropertyHelper.getInt("radio.panid", 0xDAAA), PropertyHelper.getInt("radio.panid", 0xDAAB),
			PropertyHelper.getInt("radio.panid", 0xDAAC), PropertyHelper.getInt("radio.panid", 0xDAAD),
			PropertyHelper.getInt("radio.panid", 0xDAAE) };

// Sensor1
	private static final String sensorId = "Sensor1";
	private static int SENSOR_NODE_ADDRESS = node_list[0];
	// private static int baseStationAddr = node_list[0];
	private static int[] nextNode = { node_list[1]  };
	private static int[] previousNode = {  };


	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(sensorId + " Waiting for task..");
		runs();
	}

	/**
	 * 
	 */
	public static void runs() {
		try {
			AT86RF231 t = Node.getInstance().getTransceiver();
			t.open();
			t.setAddressFilter(COMMON_PANID, SENSOR_NODE_ADDRESS, SENSOR_NODE_ADDRESS, false);
			final RadioDriver radioDriver = new AT86RF231RadioDriver(t);
			final FrameIO fio = new RadioDriverFrameIO(radioDriver);
			receive(fio);
			send("Hello World!!", SENSOR_NODE_ADDRESS, nextNode[0], fio);
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @param fio
	 */
	public static void receive(final FrameIO fio) {
		Thread thread = new Thread() {
			public void run() {
				Frame frame = new Frame();
				while (true) {
					try {
						fio.receive(frame);
						byte[] content = frame.getPayload();
						String str = new String(content, 0, content.length);
						System.out.println(str);
					} catch (IOException e) {
					} 
				}
			}
		};
		thread.start();
	}

	/**
	 * 
	 * @param msg
	 * @param src
	 * @param dest
	 * @param fio
	 */
	public static void send(String msg, int src, int dest, FrameIO fio) {
		int frameControl = Frame.TYPE_DATA | Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.ACK_REQUEST
				| Frame.SRC_ADDR_16;
		final Frame sentFrame = new Frame(frameControl);
		sentFrame.setDestPanId(COMMON_PANID);
		sentFrame.setDestAddr(dest);
		sentFrame.setSrcAddr(src);
		sentFrame.setPayload(msg.getBytes());
		try {
			fio.transmit(sentFrame);
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @param msg
	 * @param fio
	 */
	private static void forwardMsgToNextNode(String msg, FrameIO fio) {
		for (int i = 0; i < nextNode.length; i++) {
			send(msg, SENSOR_NODE_ADDRESS, nextNode[i], fio);
		}
	}

	/**
	 * 
	 * @param msg
	 * @param fio
	 */
	private static void forwardMsgToPreviousNode(String msg, FrameIO fio) {
		for (int i = 0; i < previousNode.length; i++) {
			send(msg, SENSOR_NODE_ADDRESS, previousNode[i], fio);
		}
	}

}
