package com.dhaalves.weka;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Range;
import weka.core.Utils;
import weka.experiment.*;
import weka.experiment.Experiment;

import javax.swing.*;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Experimenter {

    public static void run(String[] args) throws Exception {
        // parameters provided?
        if (args.length == 0) {
            System.out.println(
                    "\nUsage: ExperimentDemo\n"
                            + "\t   -c <classifier incl. parameters>\n"
                            + "\t   -exptype <classification|regression>\n"
                            + "\t   -splittype <crossvalidation|randomsplit>\n"
                            + "\t   -runs <# of runs>\n"
                            + "\t   -folds <folds for CV>\n"
                            + "\t   -percentage <percentage for randomsplit>\n"
                            + "\t   -result <ARFF file for storing the results>\n"
                            + "\t   -d dataset (can be supplied multiple times)\n");
            System.exit(1);
        }

        // 1. setup the experiment
        System.out.println("Setting up...");
        Experiment exp = new Experiment();
        exp.setPropertyArray(new Classifier[0]);
        exp.setUsePropertyIterator(true);

        String option;

        // classification or regression
        option = Utils.getOption("exptype", args);
        if (option.length() == 0)
            option = "classification";
//            throw new IllegalArgumentException("No experiment type provided!");

        SplitEvaluator se = null;
        Classifier sec = null;
        boolean classification = false;
        if (option.equals("classification")) {
            classification = true;
            se = new ClassifierSplitEvaluator();
            sec = ((ClassifierSplitEvaluator) se).getClassifier();
        } else if (option.equals("regression")) {
            se = new RegressionSplitEvaluator();
            sec = ((RegressionSplitEvaluator) se).getClassifier();
        } else {
            throw new IllegalArgumentException("Unknown experiment type '" + option + "'!");
        }

        // crossvalidation or randomsplit
        option = Utils.getOption("splittype", args);
        if (option.length() == 0)
            option = "crossvalidation";
//            throw new IllegalArgumentException("No split type provided!");

        if (option.equals("crossvalidation")) {
            CrossValidationResultProducer cvrp = new CrossValidationResultProducer();
            option = Utils.getOption("folds", args);
            if (option.length() == 0)
                option = "10";
//                throw new IllegalArgumentException("No folds provided!");
            cvrp.setNumFolds(Integer.parseInt(option));
            cvrp.setSplitEvaluator(se);

            PropertyNode[] propertyPath = new PropertyNode[2];
            try {
                propertyPath[0] = new PropertyNode(
                        se,
                        new PropertyDescriptor("splitEvaluator",
                                CrossValidationResultProducer.class),
                        CrossValidationResultProducer.class);
                propertyPath[1] = new PropertyNode(
                        sec,
                        new PropertyDescriptor("classifier",
                                se.getClass()),
                        se.getClass());
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }

            exp.setResultProducer(cvrp);
            exp.setPropertyPath(propertyPath);

        } else if (option.equals("randomsplit")) {
            RandomSplitResultProducer rsrp = new RandomSplitResultProducer();
            rsrp.setRandomizeData(true);
            option = Utils.getOption("percentage", args);
            if (option.length() == 0)
                throw new IllegalArgumentException("No percentage provided!");
            rsrp.setTrainPercent(Double.parseDouble(option));
            rsrp.setSplitEvaluator(se);

            PropertyNode[] propertyPath = new PropertyNode[2];
            try {
                propertyPath[0] = new PropertyNode(
                        se,
                        new PropertyDescriptor("splitEvaluator",
                                RandomSplitResultProducer.class),
                        RandomSplitResultProducer.class);
                propertyPath[1] = new PropertyNode(
                        sec,
                        new PropertyDescriptor("classifier",
                                se.getClass()),
                        se.getClass());
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }

            exp.setResultProducer(rsrp);
            exp.setPropertyPath(propertyPath);
        } else {
            throw new IllegalArgumentException("Unknown split type '" + option + "'!");
        }

        // runs
        option = Utils.getOption("runs", args);
        if (option.length() == 0)
            option = "10";
//            throw new IllegalArgumentException("No runs provided!");
        exp.setRunLower(1);
        exp.setRunUpper(Integer.parseInt(option));

        // classifier
        option = Utils.getOption("c", args);
        if (option.length() == 0)
            throw new IllegalArgumentException("No classifier provided!");
        List<Classifier> classifierList = new ArrayList<>();
        String classifier = "";
        for (String classname : option.split(";")) {
            classifier = classname;
            classifierList.add((Classifier) Utils.forName(Classifier.class, classname, null));
        }
        exp.setPropertyArray(classifierList.toArray(new Classifier[0]));

//        String[] options = Utils.splitOptions(option);
//        String classname = options[0];
//        options[0] = "";
//        Classifier c = (Classifier) Utils.forName(Classifier.class, classname, options);
//        exp.setPropertyArray(new Classifier[]{c});

        // datasets
        String filename = "";
        boolean data = false;
        DefaultListModel model = new DefaultListModel();
        do {
            option = Utils.getOption("d", args);
            if (option.length() > 0) {
                filename = Paths.get(option).getFileName().toString().replace(".arff", "");
                File file = new File(option);
                if (!file.exists())
                    throw new IllegalArgumentException("File '" + option + "' does not exist!");
                data = true;
                model.addElement(file);
            }
        }
        while (option.length() > 0);
        if (!data)
            throw new IllegalArgumentException("No data files provided!");
        exp.setDatasets(model);

        // result
        String result_file = Utils.getOption("result", args);
//        if (option.length() == 0)
//            option = "result-raw-experimenter.arff";
////            throw new IllegalArgumentException("No result file provided!");
        InstancesResultListener irl = new InstancesResultListener();
        irl.setOutputFile(new File("results_weka.arff"));
        exp.setResultListener(irl);
        // 2. run experiment
        System.out.println("Initializing...");
        exp.initialize();
        System.out.println("Running...");
        exp.runExperiment(true);
        System.out.println("Finishing...");
        exp.postProcess();

        // 3. calculate statistics and output them
        System.out.println("Evaluating...");
        PairedTTester tester = new PairedCorrectedTTester();
        Instances result = new Instances(new BufferedReader(new FileReader(irl.getOutputFile())));
        tester.setInstances(result);
        tester.setSortColumn(-1);
        tester.setRunColumn(result.attribute("Key_Run").index());
        if (classification)
//            tester.setFoldColumn(result.attribute("Key_Fold").index());
            tester.setResultsetKeyColumns(
                    new Range(
                            ""
                                    + (result.attribute("Key_Dataset").index() + 1)));
        tester.setDatasetKeyColumns(
                new Range(
                        ""
                                + (result.attribute("Key_Scheme").index() + 1)
                                + ","
                                + (result.attribute("Key_Scheme_options").index() + 1)
                                + ","
                                + (result.attribute("Key_Scheme_version_ID").index() + 1)));
        tester.setResultMatrix(new ResultMatrixPlainText());
        tester.setDisplayedResultsets(null);
        tester.setSignificanceLevel(0.05);
        tester.setShowStdDevs(true);
        // fill result matrix (but discarding the output)
        if (classification)
            tester.multiResultsetFull(0, result.attribute("Percent_correct").index());
        else
            tester.multiResultsetFull(0, result.attribute("Correlation_coefficient").index());
        // output results for reach dataset
        System.out.println("\nResult:");
        ResultMatrix matrix = tester.getResultMatrix();
        for (int i = 0; i < matrix.getColCount(); i++) {
            String acc = String.format(Locale.ENGLISH, "%.2f", matrix.getMean(i, 0));
            String std = String.format(Locale.ENGLISH, "%.2f", matrix.getStdDev(i, 0));
            System.out.println(filename);
            System.out.println("    Accuracy: " + acc);
            System.out.println("    StdDev: " + std);

            String line = filename + "," + classifier + "," + acc + "," + std + "\n";
//            Files.write(Paths.get(result_file + File.separator + "results_weka.txt"), line.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
    }
}