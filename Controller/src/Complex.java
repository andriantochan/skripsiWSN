public class Complex {

    public double real;
    public double imaginer;

    public Complex(double re, double img) {
        this.real = re;
        this.imaginer = img;
    }

    public void setReal(double r) {
        this.real = r;
    }

    public void setImaginer(double i) {
        this.imaginer = i;
    }

    public double getReal() {
        return this.real;
    }

    public double getImaginer() {
        return this.imaginer;
    }

    public Complex add(Complex c) {
        Complex result = new Complex(this.real + c.real, this.imaginer + c.imaginer);
        return result;
    }

    public Complex minus(Complex c) {
        Complex result = new Complex(this.real - c.real, this.imaginer - c.imaginer);
        return result;
    }

    public Complex multiplication(Complex c) {
        Complex result = new Complex(this.real * c.real - this.imaginer * c.imaginer, this.real * c.imaginer + this.imaginer * c.real);
        return result;
    }

    public double absolute() {
        double h = Math.sqrt(Math.pow(this.real, 2) + Math.pow(this.imaginer, 2));
        return h;
    }

    public String toString() {
        return this.real + ", " + this.imaginer;
    }
}
