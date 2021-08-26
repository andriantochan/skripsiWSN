/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//import org.apache.tools.ant.BuildException;
//import org.apache.tools.ant.DefaultLogger;
//import org.apache.tools.ant.Project;
//import org.apache.tools.ant.ProjectHelper;
//
//import com.virtenio.commander.io.DataConnection;
//import com.virtenio.commander.toolsets.preon32.Preon32Helper;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import com.virtenio.commander.io.DataConnection;
import com.virtenio.commander.toolsets.preon32.Preon32Helper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Sensor extends Application {

    public static void main(String[] args) {
        Sensor.launch(args);
    }
    private Thread threadSensing;
    private BufferedInputStream bufferedInput;
    private DataConnection dataCon;
    private boolean sensing;
    private HashMap<String, StartChart> chartAmp = new HashMap<String, StartChart>();

    ;

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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
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
    public StartChart getChart(String sensorId) {
        if (!chartAmp.containsKey(sensorId)) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    StartChart sc = new StartChart(sensorId);
                    sc.start();
                    chartAmp.put(sensorId, sc);
                }
            });
        }
        while(!chartAmp.containsKey(sensorId)) {
        	
        }
        return chartAmp.get(sensorId);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        new Thread() {
            @Override
            public void run() {

                Long current = System.currentTimeMillis();
                //Input User	
                try {
                	Scanner sc = new Scanner(System.in);
                	context_set("context.set.1");
                	time_synchronize();
                	
                	/*
                	 * Com nya khusus basestation
                	 * module nama sama
                	 */
                	Preon32Helper nodeHelper = new Preon32Helper("COM5", 115200);
                	dataCon = nodeHelper.runModule("basestation2");
                	bufferedInput = new BufferedInputStream(dataCon.getInputStream());
                	dataCon.flush();
                    while (true) {
                        System.out.println("1. Check Node Online Status");
                        System.out.println("2. Start Sensing");
                        System.out.println("3. Stop Sensing");
                        System.out.println("4. Exit Program");
                        int masukan = sc.nextInt();
                        if (sensing) {
                            if (masukan == 1 || masukan == 2 || masukan == 4) {
                                System.out.println("Still in Sensing State!!");
                                masukan = 0;
                            } else {
                        		dataCon.write(masukan);
                            }
                        } else {
                        	dataCon.write(masukan);
                        }
                        byte[] buffer = new byte[1024];
                        if (masukan == 1) {
                            System.out.println("Online Node : ");
                            Thread.sleep(500);
			                while (bufferedInput.available() > 0) {
			                    bufferedInput.read(buffer);
			                    dataCon.flush();
			                    
			                    String temp = new String(buffer).trim();
			                    String[] lines = temp.split("\n");
			                    for(String s : lines) {
			                    	String[] words = s.split(" ");
			                    	if(words[0].charAt(0) == '1') {

				                    	System.out.printf("%s %s %s\n",words[0],words[1],timeFormat(current));
			                    	}else {

				                    	System.out.printf("%s %s %s\n",words[0],words[1],timeFormat(current));
			                    	}
			                    }
			                }
                        } else if (masukan == 2) {
                            System.out.println("Sensing...");
                            sensing = true;

                            if (threadSensing == null) {
                                threadSensing = new Thread() {
                                    public void run() {
                                        while (sensing) {
                                            try {
			                                    if (bufferedInput.available() > 0) {
			                                    	byte[] bytes = new byte[1024];
			                                        BufferedReader br = new BufferedReader(new InputStreamReader(dataCon.getInputStream()));
			                                        dataCon.flush();
			                                        String temp = br.readLine();
//			                                        System.out.println(temp);
			                                        String[] hasil = temp.split(",");
			                                        if (hasil[0].charAt(0) == '2') {
			                                            String sensorid = hasil[0].substring(1);
			                                            final StartChart ct = getChart(sensorid);
														ct.addData(hasil);
			                                        }
			                                        br.close();
			                                    }
			                                } catch (IOException e) {
			                                	System.out.println(e.getMessage());
			                                }
			                            }
			                        }
			                    };
			                }
                            threadSensing.start();
                        } else if (masukan == 3) {
                            if (sensing) {
                                System.out.println("Stopping sense..");
                                sensing = false;
                                threadSensing = null;
                                System.out.println("Sensing has been stopped!!");
                            } else if (!sensing) {
                                System.out.println("Sensor does not in sensing state!!");
                            }
                        } else if (masukan == 4) {
                            System.out.println("Terminating program..");
                            sensing = false;
                            Thread.sleep(500);
                            System.out.println("Progam has terminated!!");
                            System.exit(0);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error occured!");
                }
            }
        }.start();
    }
}
