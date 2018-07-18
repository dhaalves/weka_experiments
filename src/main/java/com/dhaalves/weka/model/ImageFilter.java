package com.dhaalves.weka.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ImageFilter {
    EH("weka.filters.unsupervised.instance.imagefilter.EdgeHistogramFilter"),
    CL("weka.filters.unsupervised.instance.imagefilter.ColorLayoutFilter"),
    PHOG("weka.filters.unsupervised.instance.imagefilter.PHOGFilter"),
    BPP("weka.filters.unsupervised.instance.imagefilter.BinaryPatternsPyramidFilter"),
    FCTH("weka.filters.unsupervised.instance.imagefilter.FCTHFilter"),
    FOH("weka.filters.unsupervised.instance.imagefilter.FuzzyOpponentHistogramFilter"),
    JC("weka.filters.unsupervised.instance.imagefilter.JpegCoefficientFilter"),
    SCH("weka.filters.unsupervised.instance.imagefilter.SimpleColorHistogramFilter"),
    G("weka.filters.unsupervised.instance.imagefilter.GaborFilter"),
    ACC("weka.filters.unsupervised.instance.imagefilter.AutoColorCorrelogramFilter");

    ImageFilter(String wekaClass) {
        this.wekaClass = wekaClass;
    }

    private String wekaClass;

    public String getWekaClass() {
        return wekaClass;
    }

    public static List<String> getWekaClasses() {
        return Stream.of(ImageFilter.values()).map(ImageFilter::getWekaClass).collect(Collectors.toList());
    }

    public static List<String> getNames() {
        return Stream.of(ImageFilter.values()).map(ImageFilter::name).collect(Collectors.toList());
    }
}
