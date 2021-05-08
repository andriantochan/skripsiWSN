import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class StartChart extends Application {
	public ArrayList<Chart> charts = new ArrayList<>();
	private SampleData sampleData = new SampleData(64);
	private DataFrequency dataFrequency = new DataFrequency();

	public SampleData getSample() {
//		if(sampleData == null) {
//			sampleData = new SampleData(64);
//		}
		return sampleData;
	}

	public DataFrequency getData() {
//		if(dataFrequency == null) {
//			dataFrequency = new DataFrequency();
//		}
		return dataFrequency;
	}

	public Scene getScene() {
		charts.add(new ChartAmplitude(this.getSample(), this.getData(), ""));
		charts.add(new ChartFrequency(this.getSample(), this.getData(), ""));
		AnchorPane ap = new AnchorPane();
		int i = 0;
		int height = 400;
		int width = 600;
		for (Chart chart : charts) {
			LineChart lc = chart.getChart();
			lc.relocate(0, height * i);
			chart.updateTitle("");
			i++;
			ap.getChildren().add(lc);
			lc.setMinSize(width, height);
			lc.setMaxSize(width, height);
		}
		return new Scene(ap, width, i * height);
	}

	public void addData(String[] hasil) {
		if (hasil[0].charAt(0) == '2') {

			// Sample Data
//			this.getSample().addX(Double.parseDouble(hasil[1]));
//			this.getSample().addY(Double.parseDouble(hasil[2]));
//			this.getSample().addZ(Double.parseDouble(hasil[3]));
			this.getSample().addX(Math.random() * 100);
			this.getSample().addY(Math.random() * 100);
			this.getSample().addZ(Math.random() * 100);

			if (this.charts.size() > 0) {
				ChartAmplitude c = (ChartAmplitude) this.charts.get(0);
				System.out.printf("%s %s %s\n", this.getSample().X.size(), this.getSample().Y.size(),
						this.getSample().Z.size());
				System.out.printf("%f %f %f\n", c.getX(), c.getY(), c.getZ());
			}
			System.out.println("proses mulai");
			String sensor = hasil[0].substring(1, hasil[0].length());

			// Perhitungan FFT
			FFT computeFFT = new FFT(this.getSample());
			computeFFT.convertComplex();
			Complex[] tempRes1 = computeFFT.fft(computeFFT.xComplex);
			Complex[] tempRes2 = computeFFT.fft(computeFFT.yComplex);
			Complex[] tempRes3 = computeFFT.fft(computeFFT.zComplex);
			double t1 = 0, t2 = 0, t3 = 0;
			DecimalFormat df = new DecimalFormat("#.####");
			for (int i = 0; i < tempRes1.length; i++) {
				t1 += tempRes1[i].absolute() / tempRes1.length;
				t2 += tempRes2[i].absolute() / tempRes2.length;
				t3 += tempRes3[i].absolute() / tempRes3.length;
			}

			this.getData().setX(Double.parseDouble(df.format(t1)));
			this.getData().setY(Double.parseDouble(df.format(t2)));
			this.getData().setZ(Double.parseDouble(df.format(t3)));

//			System.out.printf("%.4f %.4f %.4f\n",t1,t2,t3);
//			System.out.println();
			updateTitle(sensor);
		}
	}

	public void updateTitle(String sensorId) {
		for (int i = 0; i < charts.size(); i++) {
			charts.get(i).updateTitle(sensorId);
		}
	}

	public void updateDF(DataFrequency df) {
		for (int i = 0; i < charts.size(); i++) {
			charts.get(i).dataFrequency = df;
		}
	}

	public void updateSample(SampleData sd) {
		for (int i = 0; i < charts.size(); i++) {
			charts.get(i).sampleData = sd;
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Grafik Amplitudo");
		primaryStage.show();
		primaryStage.setScene(getScene());
	}

	public static void main(String[] args) {
		launch(args);
	}

}

abstract class Chart {
	public abstract LineChart getChart();

	public abstract void updateTitle(String sensorID);

	public SampleData sampleData;
	public DataFrequency dataFrequency;
	public String sensorId;

	public StartChart getCurrent() {
		return Tester.getChart(this.sensorId);
	}

	public ChartAmplitude getAmplitude() {
		return (ChartAmplitude) this.getCurrent().charts.get(0);
	}

	public ChartFrequency getFrequency() {
		return (ChartFrequency) this.getCurrent().charts.get(1);
	}
}

class ChartAmplitude extends Chart {
	final int WINDOW_SIZE = 10;
	private ScheduledExecutorService scheduledExecutorService;
	public LineChart<String, Number> lineChart;

	public ChartAmplitude(SampleData sample, DataFrequency df, String id) {
		this.sampleData = sample;
		this.sensorId = id;
		this.dataFrequency = df;
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
		y.setLabel("Value");
		y.setAnimated(false);

		// bikin line chart
		lineChart = new LineChart<>(x, y);
//		updateTitle(sampleData.sensorId);
		lineChart.setAnimated(false);

		// buat nampilin datanya
		XYChart.Series<String, Number> series1 = new XYChart.Series<>();
		series1.setName("Sumbu X");

		XYChart.Series<String, Number> series2 = new XYChart.Series<>();
		series2.setName("Sumbu Y");

		XYChart.Series<String, Number> series3 = new XYChart.Series<>();
		series3.setName("Sumbu Z");

		// add series to chart
		lineChart.getData().add(series1);
		lineChart.getData().add(series2);
		lineChart.getData().add(series3);

		// setup scenev

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

		// setup executor to put data periodically
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

		// put data per second
		scheduledExecutorService.scheduleAtFixedRate(() -> {

			// update chartnya

			Platform.runLater(() -> {
				// dapetin waktu skrng
				Date now = new Date();

				// taro random number dengan waktu skrng
				if (getCurrent() != null && getCurrent().charts != null) {
					System.out.println(getAmplitude().sampleData.X.size() + "ASKDKASDKK");
					series1.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), getAmplitude().getX()));
					series2.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), getAmplitude().getY()));
					series3.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), getAmplitude().getZ()));
				}
				if (series1.getData().size() > WINDOW_SIZE) {
					series1.getData().remove(0);
					series2.getData().remove(0);
					series3.getData().remove(0);
				}

			});
		}, 0, 1, TimeUnit.SECONDS);
		return this.lineChart;
	}

	public void updateTitle(String sensorID) {
		Platform.runLater(() -> {
			if (lineChart != null) {
				lineChart.setTitle("Grafik Amplitudo " + sensorID);
			}
		});
	}

	public double getX() {
		double hasil = 0;
		for (double b : sampleData.X) {
			hasil += b;
		}
		return hasil;
	}

	public double getY() {
		double hasil = 0;
		for (double b : sampleData.Y) {
			hasil += b;
		}
		return hasil;
	}

	public double getZ() {
		double hasil = 0;
		for (double b : sampleData.Z) {
			hasil += b;
		}
		return hasil;
	}
}

class ChartFrequency extends Chart {
	private ScheduledExecutorService scheduledExecutorService;
	final int WINDOW_SIZE = 10;
	public LineChart<String, Number> lineChart;
	static double tempX = Double.MAX_VALUE;
	static double tempY = Double.MAX_VALUE;
	static double tempZ = Double.MAX_VALUE;

	public ChartFrequency(SampleData sample, DataFrequency df, String id) {
		this.sampleData = sample;
		this.sensorId = id;
		this.dataFrequency = df;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getSensorId() {
		return this.sensorId;
	}

	public void updateTitle(String sensorID) {
		Platform.runLater(() -> {
			if (lineChart != null) {
				lineChart.setTitle("Grafik Frequency " + sensorID);
			}

		});
	}

	public LineChart<String, Number> getChart() {
//		Stage stage2 = new Stage();
//		stage2.setTitle("Grafik Frekuensi");

		// buat show di graph
//		stage2.show();

		final CategoryAxis x = new CategoryAxis(); // buat sumbu x (waktu)
		final NumberAxis y = new NumberAxis(); // buat sumbu y (hasil sensor)

		x.setLabel("Time");
		x.setAnimated(false);
		y.setLabel("Value");
		y.setAnimated(false);

		// bikin line chart
		lineChart = new LineChart<>(x, y);
//		updateTitle(dataFrequency.sensorId);
		lineChart.setAnimated(false);

		// buat nampilin datanya
		XYChart.Series<String, Number> series1 = new XYChart.Series<>();
		series1.setName("Sumbu X");

		XYChart.Series<String, Number> series2 = new XYChart.Series<>();
		series2.setName("Sumbu Y");

		XYChart.Series<String, Number> series3 = new XYChart.Series<>();
		series3.setName("Sumbu Z");

		// add series to chart
		lineChart.getData().add(series1);
		lineChart.getData().add(series2);
		lineChart.getData().add(series3);

		// setup scene
//		Scene scene = new Scene(lineChart, 800, 600);
//		stage2.setScene(scene);

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

		// setup executor to put data periodically
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

		// put data per second
		scheduledExecutorService.scheduleAtFixedRate(() -> {

			// update chartnya
			Platform.runLater(() -> {
				// dapetin waktu skrng
				Date now = new Date();

				boolean toggle = false;
				// taro random number dengan waktu skrng
				if (getCurrent() != null && getCurrent().charts != null) {

					if (tempX != getFrequency().dataFrequency.X) {
						toggle = true;
					} else if (tempY != getFrequency().dataFrequency.Y) {
						toggle = true;

					} else if (tempZ != getFrequency().dataFrequency.Z) {
						toggle = true;
					}

					if (toggle) {
						series1.getData()
								.add(new XYChart.Data<>(simpleDateFormat.format(now), getFrequency().dataFrequency.X));
						series2.getData()
								.add(new XYChart.Data<>(simpleDateFormat.format(now), getFrequency().dataFrequency.Y));
						series3.getData()
								.add(new XYChart.Data<>(simpleDateFormat.format(now), getFrequency().dataFrequency.Z));
						tempX = getFrequency().dataFrequency.X;
						tempY = getFrequency().dataFrequency.Y;
						tempZ = getFrequency().dataFrequency.Z;
					} else {
						series1.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), 0));
						series2.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), 0));
						series3.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), 0));
					}
				}
//				series1.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), ChartAmplitude.dataFrequency.X));
//				series2.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), ChartAmplitude.dataFrequency.Y));
//				series3.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), ChartAmplitude.dataFrequency.Z));

				if (series1.getData().size() > WINDOW_SIZE) {
					series1.getData().remove(0);
					series2.getData().remove(0);
					series3.getData().remove(0);
				}

			});
		}, 0, 1, TimeUnit.SECONDS);
		return this.lineChart;

	}

}