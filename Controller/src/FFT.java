	
public class FFT {
	public SampleData data;
	public Complex[] xComplex;
	public Complex[] yComplex;
	public Complex[] zComplex;
	
	public FFT(SampleData data) {
		this.data = data;
		int nearestBase2 = (int) Math.pow(2, (int) (Math.log(data.X.size())/Math.log(2)));
		System.out.println(nearestBase2);
		this.xComplex = new Complex[nearestBase2];
		this.yComplex = new Complex[nearestBase2];
		this.zComplex = new Complex[nearestBase2];
	}
	
	public void convertComplex() {
		int length = data.X.size();
		System.out.println(data.X.size());
		System.out.println(data.Y.size());
		System.out.println(data.Z.size());
		for(int i=0; i<xComplex.length; i++) {
			xComplex[i] = new Complex(data.X.get(length-1-i),0);
			yComplex[i] = new Complex(data.Y.get(length-1-i),0);
			zComplex[i] = new Complex(data.Z.get(length-1-i),0);
		}
	}
	
	public int bitReverse(int banyak, int bit) {
		if(banyak==0) {
			return 0;
		}
		
		String temp = Integer.toBinaryString(banyak);
		if(temp.length()<bit) {
			int ct = bit - temp.length();
			for(int i=0; i<ct; i++) {
				temp = "0" + temp;
			}
		}
		
		int hasil = 0;
		for(int i = temp.length()-1; i>=0; i--) {
			if(temp.charAt(i) == '1') {
				hasil += Math.pow(2, i);
			}
		}
		return hasil;
	}
	
	public Complex[] fft(Complex[] input) {
		int bit = (int) (Math.log(input.length) / Math.log(2));
		Complex[] orderFinal = new Complex[input.length];
		for(int i=0; i<input.length; i++) {
			int order = bitReverse(i,bit);
			orderFinal[i] = input[order];
		}
		for(int i=2; i<= orderFinal.length; i=i*2 ) {
			for(int j=0; j<orderFinal.length; j +=i) {
				for(int k=0; k<i/2; k++) {
					Complex awal = orderFinal[j+k];
					Complex akhir = orderFinal[j+k+(i/2)];
					
					double weight = (-2 * Math.PI * k)/ (double) i;
					Complex exponential = (new Complex(Math.cos(weight), Math.sin(weight)).multiplication(akhir));
//					System.out.println(exponential.absolute());
					orderFinal[j+k] = awal.add(exponential);
					orderFinal[j+k+(i/2)] = awal.minus(exponential);
				}
			}
		}
		return orderFinal;
	}
	
	public void toString(Complex[] complex) {
		for (int i=0; i<complex.length; i++) {
			System.out.println(complex[i].absolute());
		}
	}
}
