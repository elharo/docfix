package com.elharo.math;

/**
 * Represents a complex number with real and imaginary parts.
 * <p>
 * This class provides methods for basic arithmetic operations (addition, subtraction,
 * multiplication, division), absolute value calculation, and standard object methods.
 * Instances of this class are immutable and thread-safe.
 * </p>
 *
 * @author ChatGPT
 * @author Elliotte Rusty Harold
 */
public class ComplexNumber implements Cloneable { 
    /**
     * The real part of the complex number.
     */
    private final double real;

    /**
     * The imaginary part of the complex number.
     */
    private final double imaginary;

    /** The phase (angle) of the complex number in radians. */
    private double phase;

    /**
     * Constructs a complex number with the specified real and imaginary parts.
     *
     * @param real The real part
     * @param imaginary The imaginary part
     */
    public ComplexNumber(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    /**
     * Returns the real part of this complex number.
     *
     * @return The real part
     */
    public double getReal() {
        return real;
    }

    /**
     * Returns the imaginary part of this complex number.
     *
     * @return the imaginary part
     */
    public double getImaginary() {
        return imaginary;
    }

    /**
     * Adds the specified complex number to this complex number.
     *
     * @param other the complex number to add
     * @return the sum as a new ComplexNumber
     */
    public ComplexNumber add(ComplexNumber other) {
        return new ComplexNumber(this.real + other.real, this.imaginary + other.imaginary);
    }

    /**
     * Subtracts the specified complex number from this complex number.
     *
     * @param other the complex number to subtract
     * @return the difference as a new ComplexNumber
     */
    public ComplexNumber subtract(ComplexNumber other) {
        return new ComplexNumber(this.real - other.real, this.imaginary - other.imaginary);
    }

    /**
     * Multiplies this complex number by the specified complex number.
     *
     * @param other the complex number to multiply by
     * @return the product as a new ComplexNumber
     */
    public ComplexNumber multiply(ComplexNumber other) {
        double realPart = this.real * other.real - this.imaginary * other.imaginary;
        double imaginaryPart = this.real * other.imaginary + this.imaginary * other.real;
        return new ComplexNumber(realPart, imaginaryPart);
    }

    /**
     * Divides this complex number by the specified complex number.
     *
     * @param other the complex number to divide by
     * @return the quotient as a new ComplexNumber
     * @throws ArithmeticException if the divisor is zero
     */
    public ComplexNumber divide(ComplexNumber other) {
        double denominator = other.real * other.real + other.imaginary * other.imaginary;
        if (denominator == 0) { // Check for single line comment preservation
            throw new ArithmeticException("Division by zero");
        }
        double realPart = (this.real * other.real + this.imaginary * other.imaginary) / denominator;
        double imaginaryPart = (this.imaginary * other.real - this.real * other.imaginary) / denominator;
        return new ComplexNumber(realPart, imaginaryPart);
    }

    /**
     * Returns the absolute value (magnitude) of this complex number.
     *
     * @return the absolute value
     */
    public double abs() {
        return Math.sqrt(real * real + imaginary * imaginary);
    }

    /**
     * Returns a string representation of this complex number.
     *
     * @return a string in the format "a + bi" or "a - bi"
     */
    @Override
    public String toString() {
        if (imaginary >= 0) {
            return real + " + " + imaginary + "i";
        } else {
            return real + " - " + (-imaginary) + "i";
        }
    }

    /**
     * Compares this complex number with the specified object for equality.
     *
     * @param obj the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ComplexNumber that = (ComplexNumber) obj;
        return Double.compare(that.real, real) == 0 && 
               Double.compare(that.imaginary, imaginary) == 0;
    }

    /**
     * Returns a hash code value for this complex number.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Double.hashCode(real) * 31 + Double.hashCode(imaginary);
    }

    /**
     * Creates and returns a copy of this complex number.
     *
     * @return a clone of this instance
     */
    @Override
    public ComplexNumber clone() {
        try {
            return (ComplexNumber) super.clone();
        } catch (CloneNotSupportedException e) {
            // This shouldn't happen since we implement Cloneable
            throw new AssertionError();
        }
    }
}