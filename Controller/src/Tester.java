import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import com.virtenio.commander.io.DataConnection;
import com.virtenio.commander.toolsets.preon32.Preon32Helper;


public class Tester {
	
	private static Thread threadSensing;
	private static BufferedInputStream bufferedInput;
	private static DataConnection dataCon;
	private static boolean sensing;
	private static HashMap<String,GraphAmplitude> graphAmp;
	private static HashMap<String,ChartAmplitude> chartAmp;
	private static HashMap<String,GraphFrequency> graphFreq;
	
	
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
					System.out.println();
					System.out.println(temp);
				}
			}
			
			else if(masukan==2) {
				System.out.println("Sensing...");
				sensing=true;
				graphFreq = new HashMap<String,GraphFrequency>();
				graphAmp = new HashMap<String,GraphAmplitude>();
				chartAmp = new HashMap<String, ChartAmplitude>();
				
				for (int i =1; i<=5;i++) {
					i = i+64;
					chartAmp.put("Sensor"+(char)i, new ChartAmplitude());
					
				}
				ChartAmplitude ct = chartAmp.get("Sensor"+(char)1);
				Thread t2 = new Thread() {
					public void run() {
						ct.sampleData = new SampleData(64);
						ct.main(args);
					}
				};
				t2.start();
				
				if(threadSensing==null) {
					threadSensing = new Thread() {
						public void run() {
							SampleData data = new SampleData(64);
							int counter=0;
							Complex[] res = new Complex[64];
							while(sensing) {
								byte[] buffer = new byte[1024];
								try {
									if(bufferedInput.available()>0) {
										bufferedInput.read(buffer);
										dataCon.flush();
										String temp = new String(buffer);
										System.out.println(temp);
										String[] hasil = temp.split(",");
										
										
										if(hasil[0].charAt(0)=='2') {
											
											//Sample Data
											data.sensorId = hasil[0];
											data.setX(Double.parseDouble(String.format("%.4f", Double.parseDouble(hasil[1]))),counter);
											data.setY(Double.parseDouble(String.format("%.4f", Double.parseDouble(hasil[2]))),counter);
											data.setZ(Double.parseDouble(String.format("%.4f", Double.parseDouble(hasil[3]))),counter);

											
											counter+=1;
											
											if(counter == 64) {
												counter = 0;
												ct.sampleData = data;
											}
//											
//											if(counter==64) {
//												System.out.println("proses mulai");
//												String sensor = hasil[0].substring(1,hasil[0].length());
//												//Perhitungan FFT
//												FFT computeFFT = new FFT(64, data);
//												computeFFT.convertComplex();
//												Complex[] tempRes = computeFFT.fft(computeFFT.xComplex);		
//												for(int i=0;i<tempRes.length;i++) {
//													if(!Double.isNaN(tempRes[i].absolute())) {
//														System.out.println(tempRes[i].absolute());
//														graphAmp.get(sensor).plotMaker.realTimeVisual(i+"", tempRes[i].absolute()+"");
//														graphAmp.get(sensor).canPlot=true;
//													}
//												}
//												
//												counter=0;
//											}
											
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

}


