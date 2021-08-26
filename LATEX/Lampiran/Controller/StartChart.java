import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StartChart {

    public ArrayList<Chart> charts = new ArrayList<>();
    private SampleData sampleData = new SampleData(64);
    private DataFrequency dataFrequency = new DataFrequency();
    private Stage stage = new Stage();
    private String sensorId;
    

    public StartChart(String sensorId){
        this.sensorId = sensorId;
    }

    public SampleData getSample() {
        return sampleData;
    }

    public DataFrequency getData() {
        return dataFrequency;
    }

    public Scene getScene() throws IOException {
        charts.add(new ChartAmplitude(this.getSample(), this.getData(), sensorId));
        this.getSample().setRender(charts.get(0));
        charts.add(new ChartFrequency(this.getSample(), this.getData(), sensorId));
        this.getData().setRender(charts.get(1));
        AnchorPane ap = new AnchorPane();
        int i = 0;
        int height = 400;
        int width = 600;
        for (Chart chart : charts) {
            LineChart lc = chart.getChart();
            lc.relocate(0, height * i);
            i++;
            ap.getChildren().add(lc);
            lc.setMinSize(width, height);
            lc.setMaxSize(width, height);
        }
        return new Scene(ap, width, i * height);
    }

    public void addData(String[] hasil) throws IOException {
        if (hasil[0].charAt(0) == '2') {
            // Sample Data
            this.getSample().addX(Double.parseDouble(hasil[1]));
            this.getSample().addY(Double.parseDouble(hasil[2]));
            this.getSample().addZ(Double.parseDouble(hasil[3]));

            // Perhitungan FFT
            FFT computeFFT = new FFT(this.getSample());
            computeFFT.convertComplex();
            Complex[] tempRes1 = computeFFT.fft(computeFFT.xComplex);
            Complex[] tempRes2 = computeFFT.fft(computeFFT.yComplex);
            Complex[] tempRes3 = computeFFT.fft(computeFFT.zComplex);
            double t1 = 0, t2 = 0, t3 = 0;
            DecimalFormat df = new DecimalFormat("#.####");
            for (int i = 0; i < tempRes1.length; i++) {
                t1 += tempRes1[i].absolute();
                t2 += tempRes2[i].absolute();	
                t3 += tempRes3[i].absolute();
            }
            t1/= tempRes1.length;
            t2/= tempRes2.length;
            t3/= tempRes3.length;
            System.out.printf("%f %f %f\n",t1,t2,t3);
            this.getData().setX(Double.parseDouble(df.format(t1)));
            this.getData().setY(Double.parseDouble(df.format(t2)));
            this.getData().setZ(Double.parseDouble(df.format(t3)));
            
        }
    }

    public void start(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.setTitle(sensorId);
                stage.show();
                try {
					stage.setScene(getScene());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
    }
}

abstract class Chart implements RenderChart {

    public abstract LineChart getChart() throws IOException;

    public SampleData sampleData;
    public DataFrequency dataFrequency;
    public String sensorId;
    public BufferedWriter writer;

    // buat nampilin datanya
    XYChart.Series<String, Number> series1 = new XYChart.Series<>();

    XYChart.Series<String, Number> series2 = new XYChart.Series<>();

    XYChart.Series<String, Number> series3 = new XYChart.Series<>();

    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
}

class ChartAmplitude extends Chart {

    final int WINDOW_SIZE = 10;
    private ScheduledExecutorService scheduledExecutorService;
    public LineChart<String, Number> lineChart;
    public ChartAmplitude(SampleData sample, DataFrequency df, String id) throws IOException {
        this.sampleData = sample;
        this.sensorId = id;
        this.dataFrequency = df;	
        this.writer = new BufferedWriter(new FileWriter("hasilXYZ.txt"));
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorId() {
        return this.sensorId;
    }

    public LineChart<String, Number> getChart() throws IOException {
        final CategoryAxis x = new CategoryAxis(); // buat sumbu x (waktu)
        final NumberAxis y = new NumberAxis(); // buat sumbu y (hasil sensor)

        x.setLabel("Time");
        x.setAnimated(false);
        y.setLabel("Value (g)");
        y.setAnimated(false);

        // bikin line chart
        lineChart = new LineChart<>(x, y);
        lineChart.setAnimated(false);

        series1.setName("Sumbu X");
        series2.setName("Sumbu Y");
        series3.setName("Sumbu Z");
        
        // add series to chart
        lineChart.getData().add(series1);
        lineChart.getData().add(series2);
        lineChart.getData().add(series3);

        // setup scene

        // setup executor to put data periodically
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        render();
        return this.lineChart;
    }

    
    public void render() throws IOException{
    	
    	Platform.runLater(new Runnable() {
			
			@Override
			public void run() {

		    	Date now = new Date();
		        lineChart.setTitle("Grafik Amplitudo " +sensorId);
		        
		        try {
					String baris = sensorId+", "+getX()+", "+getY()+", "+getZ();
					writer.write(baris);
					writer.newLine();
					writer.flush();
				}
				catch(Exception e) {
					e.getMessage();
				}
		        // taro random number dengan waktu skrng
		        series1.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), getX()));
		        series2.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), getY()));
		        series3.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), getZ()));
		        if (series1.getData().size() > WINDOW_SIZE) {
		            series1.getData().remove(0);
		            series2.getData().remove(0);
		            series3.getData().remove(0);
		        }
		        
				
			}
		});
    }
    public double getX() {
        double hasil = 0;
        for (double b : sampleData.X) {
            hasil += b;
        }
        return hasil/sampleData.X.size();
    }

    public double getY() {
        double hasil = 0;
        for (double b : sampleData.Y) {
            hasil += b;
        }
        return hasil/sampleData.Y.size();
    }

    public double getZ() {
        double hasil = 0;
        for (double b : sampleData.Z) {
            hasil += b;
        }
        return hasil/sampleData.Z.size();
    }
}

class ChartFrequency extends Chart {

    private ScheduledExecutorService scheduledExecutorService;
    final int WINDOW_SIZE = 10;
    public LineChart<String, Number> lineChart;
    public static double tempX = Double.MAX_VALUE;
    public static double tempY = Double.MAX_VALUE;
    public static double tempZ = Double.MAX_VALUE;

    public ChartFrequency(SampleData sample, DataFrequency df, String id) throws IOException{
        this.sampleData = sample;
        this.sensorId = id;
        this.dataFrequency = df;
        this.writer = new BufferedWriter(new FileWriter("hasilFFT.txt"));
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorId() {
        return this.sensorId;
    }

    public LineChart<String, Number> getChart() {
        final CategoryAxis x = new CategoryAxis(); // buat sumbu x (waktu)
        final NumberAxis y = new NumberAxis(); // buat sumbu y (hasil sensor)

        x.setLabel("Time");
        x.setAnimated(false);
        y.setLabel("Value (Hz)");
        y.setAnimated(false);

        // bikin line chart
        lineChart = new LineChart<>(x, y);
        lineChart.setAnimated(false);

        // buat nampilin datanya
        series1.setName("Sumbu X");

        series2.setName("Sumbu Y");

        series3.setName("Sumbu Z");

        // add series to chart
        lineChart.getData().add(series1);
        lineChart.getData().add(series2);
        lineChart.getData().add(series3);

        render();
        return this.lineChart;

    }
    
    public void render() {
    	Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				lineChart.setTitle("Grafik Frekuensi " +sensorId);
		        Date now = new Date();
		        
		        try {
					String baris = sensorId+", "+dataFrequency.X+", "+dataFrequency.Y+", "+dataFrequency.Z;
					writer.write(baris);
					writer.newLine();
					writer.flush();
				}
				catch(Exception e) {
					e.getMessage();
				}
		        series1.getData()
		                .add(new XYChart.Data<>(simpleDateFormat.format(now), dataFrequency.X));
		        series2.getData()
		                .add(new XYChart.Data<>(simpleDateFormat.format(now), dataFrequency.Y));
		        series3.getData()
		                .add(new XYChart.Data<>(simpleDateFormat.format(now), dataFrequency.Z));
		        tempX = dataFrequency.X;
		        tempY = dataFrequency.Y;
		        tempZ = dataFrequency.Z;
		        if (series1.getData().size() > WINDOW_SIZE) {
		            series1.getData().remove(0);
		            series2.getData().remove(0);
		            series3.getData().remove(0);
		        }
				
			}
		});
    }

}
