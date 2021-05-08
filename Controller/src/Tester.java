import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import com.virtenio.commander.io.DataConnection;
import com.virtenio.commander.toolsets.preon32.Preon32Helper;

import javafx.application.Application;
import javafx.stage.Stage;


public class Tester extends Application{
	
	private static Thread threadSensing;
	private static BufferedInputStream bufferedInput;
	private static DataConnection dataCon;
	private static boolean sensing;
	private static HashMap<String,StartChart> chartAmp;
	private static Boolean[] status =  {false,false,false,false,false};
	
	public static StartChart getChart(String sensorid) {
		return chartAmp.get(sensorid);
	}
	
	public static void main(String[] args) throws Exception {
		
		Tester tester = new Tester();
		tester.context_set("context.set.1");
		tester.time_synchronize();
		
		/*
		 * Com nya khusus basestation
		 * module nama sama
		 */
		Preon32Helper nodeHelper = new Preon32Helper("COM6", 115200);
		dataCon = nodeHelper.runModule("basestation");
		bufferedInput = new BufferedInputStream(dataCon.getInputStream());
		dataCon.flush();
		//Input User
		Scanner sc = new Scanner(System.in);
		
		while(true) {
			System.out.println("1. Check Node Online Status");
			System.out.println("2. Start Sensing");
			System.out.println("3. Stop Sensing");
			System.out.println("4. Exit Program");
			int masukan = sc.nextInt();
			if(sensing) {
				if(masukan==1 || masukan==2 || masukan==4) {
					System.out.println("Still in Sensing State!!");
					masukan = 0;
				}
				else {
					dataCon.write(masukan);
				}
			}
			else {
				dataCon.write(masukan);
			}
			
			byte[] buffer = new byte[1024];
			if(masukan==1) {
				System.out.println("Online Node : ");
				Thread.sleep(500);
				while(bufferedInput.available()>0) {
					bufferedInput.read(buffer);
					dataCon.flush();
					
					String temp = new String(buffer);
					String[] temp2 = temp.split(" ");
					String temp_time = temp2[3].substring(0, 12);
					long time = Long.parseLong(temp_time);
					System.out.println();
					System.out.println(temp2[0] + temp2[1] + "Time:" + timeFormat(time) + temp2[3].substring(12));
				}
			}
			
			else if(masukan==2) {
				System.out.println("Sensing...");
				sensing=true;
				chartAmp = new HashMap<String, StartChart>();
				
				for (int i =1; i<=5;i++) {
					i = i+64;
					StartChart ct = new StartChart();
					chartAmp.put("Sensor"+(char)i, ct);
//					new Thread() {
//						public void run() {
//						chartAmp.get("Sensor"+(char) 1).main(args);
//						ct.charts.add(new ChartAmplitude(ct.getSample(), ct.getData(), ""));
//						ct.charts.add(new ChartFrequency(ct.getSample(), ct.getData(), ""));
//						}
//					}.start();
				}
				
				if(threadSensing==null) {
					
					threadSensing = new Thread() {
						public void run() {
							while(sensing) {
								byte[] buffer = new byte[1024];
								try {
									if(bufferedInput.available()>0) {
										bufferedInput.read(buffer);
										dataCon.flush();
										String temp = new String(buffer);
										String[] hasil = temp.split(",");
										if(hasil[0].charAt(0)=='2') {
											String sensorid = hasil[0].substring(1);
											chartAmp.get(sensorid).addData(hasil);
											int i = sensorid.charAt(sensorid.length()-1) - 'A';
											if(status[i] == false) {
												status[i] = true;
												final StartChart ct = chartAmp.get(sensorid);
												new Thread() {
													public void run() {
														ct.main(args);	
													}
												}.start();
												System.out.println(ct.charts.size());
											}
											System.out.println(chartAmp.get(sensorid).charts.size());
											System.out.println(sensorid);
										}
										
									}
								}
								catch(NumberFormatException e) {
									
								}
								catch(IOException e) {
									
								}
							}
						}
					};
				}
				threadSensing.start();
			}
			
			else if(masukan==3) {
				if(sensing) {
					System.out.println("Stopping sense..");
					sensing=false;
					threadSensing=null;
					System.out.println("Sensing has been stopped!!");
				}
				else if (!sensing){
					System.out.println("Sensor does not in sensing state!!");
				}
			}
			
			else if(masukan==4) {
				System.out.println("Terminating program..");
				sensing=false;
				Thread.sleep(500);
				System.out.println("Progam has terminated!!");
				System.exit(0);
			}
		}
	}
	
	// console build ant
		private static DefaultLogger getConsoleLogger() {
			DefaultLogger consoleLogger = new DefaultLogger();
			consoleLogger.setErrorPrintStream(System.err);
			consoleLogger.setOutputPrintStream(System.out);
			consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
			return consoleLogger;
		}
		
		private static String timeFormat(long timeMillis) {
			Date date = new Date(timeMillis);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
			return simpleDateFormat.format(date);
		}

		// ant build time synchronize
		private void time_synchronize() throws Exception {
			DefaultLogger consoleLogger = getConsoleLogger();
			File buildFile = new File("E:\\Skripsi\\Skripsi_WSN\\Sandbox\\build.xml");
			Project antProject = new Project();
			antProject.setUserProperty("ant.file", buildFile.getAbsolutePath());
			antProject.addBuildListener(consoleLogger);
			try {
				antProject.fireBuildStarted();
				antProject.init();
				ProjectHelper helper = ProjectHelper.getProjectHelper();
				antProject.addReference("ant.ProjectHelper", helper);
				helper.parse(antProject, buildFile);
				String target = "cmd.time.synchronize";
				antProject.executeTarget(target);
				antProject.fireBuildFinished(null);
			} catch (BuildException e) {
			}
		}

		// set context basestation
		private void context_set(String target) throws Exception {
			DefaultLogger consoleLogger = getConsoleLogger();
			File buildFile = new File("E:\\Skripsi\\Skripsi_WSN\\Sandbox\\buildUser.xml");
			Project antProject = new Project();
			antProject.setUserProperty("ant.file", buildFile.getAbsolutePath());
			antProject.addBuildListener(consoleLogger);
			try {
				antProject.fireBuildStarted();
				antProject.init();
				ProjectHelper helper = ProjectHelper.getProjectHelper();
				antProject.addReference("ant.ProjectHelper", helper);
				helper.parse(antProject, buildFile);
				antProject.executeTarget(target);
				antProject.fireBuildFinished(null);
			} catch (BuildException e) {
			}
		}

		@Override
		public void start(Stage primaryStage) throws Exception {
			primaryStage.setTitle("Grafik Amplitudo");
			primaryStage.show();
//			primaryStage.setScene(getScene());
		}

}


