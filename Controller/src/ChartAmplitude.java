import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class ChartAmplitude extends Application{
	final int WINDOW_SIZE = 10;
	private ScheduledExecutorService scheduledExecutorService;
	public String sensorId;
	public static SampleData sampleData;
	
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getSensorId(){
		return this.sensorId;
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Grafik Amplitudo");
		
		//buat show di graph
		primaryStage.show();
		
		final CategoryAxis x = new CategoryAxis(); //buat sumbu x (waktu)
		final NumberAxis y = new NumberAxis(); //buat sumbu y (hasil sensor)
		
		x.setLabel("Time");
		x.setAnimated(false);
		y.setLabel("Value");
		y.setAnimated(false);
		
		//bikin line chart 
		final LineChart<String, Number> lineChart = new LineChart<>(x, y);
		lineChart.setTitle("Grafik Amplitudo " + sampleData.sensorId);
		lineChart.setAnimated(false);
		
		
		//buat nampilin datanya
		XYChart.Series<String, Number> series1 = new XYChart.Series<>();
		series1.setName("Sumbu X");
		
		XYChart.Series<String, Number> series2= new XYChart.Series<>();
		series2.setName("Sumbu Y");
		
		XYChart.Series<String, Number> series3 = new XYChart.Series<>();
		series3.setName("Sumbu Z");
		
		//add series to chart
		lineChart.getData().add(series1);
		lineChart.getData().add(series2);
		lineChart.getData().add(series3);
		
		//setup scene
		Scene scene = new Scene(lineChart, 800, 600);
		primaryStage.setScene(scene);
		
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
		
		//setup executor to put data periodically
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		
		//put data per second
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			//get random number 0-10
			Integer random = ThreadLocalRandom.current().nextInt(10);
			double angka = Double.MAX_VALUE;
			
			//update chartnya
			
			
			
			Platform.runLater(() -> {
				//dapetin waktu skrng
				Date now = new Date();
				
				//taro random number dengan waktu skrng
				series1.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), getX()));
				series2.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), getY()));
				series3.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), getZ()));
			
				if(series1.getData().size() > WINDOW_SIZE) {
					series1.getData().remove(0);
					series2.getData().remove(0);
					series3.getData().remove(0);
				}
				
			});
		}, 0, 1, TimeUnit.SECONDS);
		
	}
	
	public static double getX() {
		double hasil = 0;
		for(double b:sampleData.X ) {
			hasil += b;
		}
		System.out.println(hasil);
		return hasil;
	}
	
	public static double getY() {
		double hasil = 0;
		for(double b:sampleData.Y ) {
			hasil += b;
		}
		System.out.println(hasil);
		return hasil;
	}
	
	public static double getZ() {
		double hasil = 0;
		for(double b:sampleData.Z ) {
			hasil += b;
		}
		System.out.println(hasil);
		return hasil;
	}
	
	
	@Override
	public void stop() throws Exception{
		super.stop();
		scheduledExecutorService.shutdownNow();
	}
}
