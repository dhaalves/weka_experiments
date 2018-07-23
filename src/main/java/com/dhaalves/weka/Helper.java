package com.dhaalves.weka;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Helper {


    public static void removeInstancesWithAttrValue() {

    }


    public static void arrf2csv(Path ori, Path des) throws Exception {
        ArffLoader loader = new ArffLoader();
        loader.setSource(ori.toFile());
        Instances data = loader.getDataSet();

        CSVSaver saver = new CSVSaver();
        saver.setInstances(data);
        saver.setFile(des.toFile());
        saver.writeBatch();
    }

    public static void csv2arff(Path ori, Path des, Boolean hasHeader) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setNoHeaderRowPresent(!hasHeader);
        loader.setSource(ori.toFile());
        Instances data = loader.getDataSet();

        NumericToNominal filter = new NumericToNominal();
        filter.setInputFormat(data);
        filter.setAttributeIndices("last");
        data = Filter.useFilter(data, filter);

        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(des.toFile());
        saver.writeBatch();
    }

    public static boolean isArff(Path path) {
        return path.toString().endsWith(".arff");
    }

    public static boolean isCsv(Path path) {
        return path.toString().endsWith(".csv");
    }


    public static String removeFilenameExtention(String filename) {
        return filename.replaceFirst("[.][^.]+$", "");
    }


    public static void merge(Path folder) throws Exception {
        Instances merged = null;
        List<Path> collect = Files.list(folder).collect(Collectors.toList());
        for (Path path : collect) {
            ArffLoader loader = new ArffLoader();
            loader.setSource(path.toFile());
            Instances dataSet = loader.getDataSet();
            if (merged == null) {
                merged = dataSet;
            } else {
                merged.addAll(dataSet);
            }
        }


        ArffSaver saver = new ArffSaver();
        saver.setInstances(merged);
        saver.setFile(Paths.get(folder.toString() + "/merged.arff").toFile());
        saver.writeBatch();

    }

    public static void main(String[] args) throws IOException {
        String folder = "/mnt/sdb1/datasets/mammoset/exp5-2_aug";

        Files.list(Paths.get(folder)).filter(Helper::isCsv).forEach(ori -> {
            Path des = Paths.get(ori.getParent().toString() + File.separator + ori.getFileName().toString().replace("csv", "arff"));
            try {
                csv2arff(ori, des, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
