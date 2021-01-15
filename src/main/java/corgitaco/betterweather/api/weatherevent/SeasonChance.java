package corgitaco.betterweather.api.weatherevent;

public class SeasonChance {

    private double springStartWeight;
    private double springMidWeight;
    private double springEndWeight;

    private double summerStartWeight;
    private double summerMidWeight;
    private double summerEndWeight;

    private double autumnStartWeight;
    private double autumnMidWeight;
    private double autumnEndWeight;

    private double winterStartWeight;
    private double winterMidWeight;
    private double winterEndWeight;
    
    public double getSpringStartWeight() {
        return springStartWeight;
    }

    public SeasonChance setSpringStartWeight(double springStartWeight) {
        this.springStartWeight = springStartWeight;
        return this;
    }

    public double getSpringMidWeight() {
        return springMidWeight;
    }

    public SeasonChance setSpringMidWeight(double springMidWeight) {
        this.springMidWeight = springMidWeight;
        return this;
    }

    public double getSpringEndWeight() {
        return springEndWeight;
    }

    public SeasonChance setSpringEndWeight(double springEndWeight) {
        this.springEndWeight = springEndWeight;
        return this;
    }

    public double getSummerStartWeight() {
        return summerStartWeight;
    }

    public SeasonChance setSummerStartWeight(double summerStartWeight) {
        this.summerStartWeight = summerStartWeight;
        return this;
    }

    public double getSummerMidWeight() {
        return summerMidWeight;
    }

    public SeasonChance setSummerMidWeight(double summerMidWeight) {
        this.summerMidWeight = summerMidWeight;
        return this;
    }

    public double getSummerEndWeight() {
        return summerEndWeight;
    }

    public SeasonChance setSummerEndWeight(double summerEndWeight) {
        this.summerEndWeight = summerEndWeight;
        return this;
    }

    public double getAutumnStartWeight() {
        return autumnStartWeight;
    }

    public SeasonChance setAutumnStartWeight(double autumnStartWeight) {
        this.autumnStartWeight = autumnStartWeight;
        return this;
    }

    public double getAutumnMidWeight() {
        return autumnMidWeight;
    }

    public SeasonChance setAutumnMidWeight(double autumnMidWeight) {
        this.autumnMidWeight = autumnMidWeight;
        return this;
    }

    public double getAutumnEndWeight() {
        return autumnEndWeight;
    }

    public SeasonChance setAutumnEndWeight(double autumnEndWeight) {
        this.autumnEndWeight = autumnEndWeight;
        return this;
    }

    public double getWinterStartWeight() {
        return winterStartWeight;
    }

    public SeasonChance setWinterStartWeight(double winterStartWeight) {
        this.winterStartWeight = winterStartWeight;
        return this;
    }

    public double getWinterMidWeight() {
        return winterMidWeight;
    }

    public SeasonChance setWinterMidWeight(double winterMidWeight) {
        this.winterMidWeight = winterMidWeight;
        return this;
    }

    public double getWinterEndWeight() {
        return winterEndWeight;
    }

    public SeasonChance setWinterEndWeight(double winterEndWeight) {
        this.winterEndWeight = winterEndWeight;
        return this;
    }
}
