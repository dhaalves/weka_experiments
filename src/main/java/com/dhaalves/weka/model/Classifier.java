package com.dhaalves.weka.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Classifier {

    RF("weka.classifiers.trees.RandomForest"),
    SVM("weka.classifiers.functions.SMO"),
    NB("weka.classifiers.bayes.NaiveBayes"),
    KNN("weka.classifiers.lazy.IBk"),
    J48("weka.classifiers.trees.J48");

    Classifier(String wekaClass) {
        this.wekaClass = wekaClass;
    }

    private String wekaClass;

    public String getWekaClass() {
        return wekaClass;
    }

    public static List<String> getWekaClasses() {
        return Stream.of(Classifier.values()).map(Classifier::getWekaClass).collect(Collectors.toList());
    }

    public static List<String> getNames() {
        return Stream.of(Classifier.values()).map(Classifier::name).collect(Collectors.toList());
    }


}
