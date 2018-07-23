package com.dhaalves.weka;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Classify {

    private static final int CROSSVALIDATION_FOLDS = 10;
    private static final int RANDOM_SEED = 1; //ThreadLocalRandom.current().nextInt(0, 100 + 1);

    private static String ACC_PER_CLASS_FILE_NAME = "acc_per_class.csv";
    private static String CONFUSION_MATRIX_OUTPUT_DIR = "confusion_matrix";
    private static String ARFFS_FOLDER = "/mnt/sdb1/datasets/mammoset/exp5-2";


    public static void start(String arffsFolder, List<String> classifiers) throws Exception {

        MathContext mathContext = new MathContext(2, RoundingMode.HALF_UP);
        boolean writeHeader = true;
        String outputFile = arffsFolder + File.separator + ACC_PER_CLASS_FILE_NAME;
        if (Files.exists(Paths.get(outputFile))) {
            Files.delete(Paths.get(outputFile));
        }

        List<Path> paths = Files.list(Paths.get(arffsFolder)).filter(Helper::isArff).collect(Collectors.toList());
        for (Path path : paths) {
            try {
                String name = path.getFileName().toString().replaceAll(".arff", "");

                ConverterUtils.DataSource dt = new ConverterUtils.DataSource(path.toString());
                Instances instance = dt.getDataSet();
                instance.randomize(new Random(RANDOM_SEED));
                int classIndex = instance.numAttributes() - 1;
                instance.setClassIndex(classIndex);
                int num_instances = instance.size();
                int trainSize = (int) Math.round(num_instances * 0.8);
                int testSize = num_instances - trainSize;
                Instances train = new Instances(instance, 0, trainSize);
                Instances test = new Instances(instance, trainSize, testSize);
                Attribute aClass = instance.attribute("class");


                if (writeHeader) {
                    writeHeader = false;
                    Stream<String> stream = Stream.concat(Stream.of("features"), Stream.of("classifier"));
                    stream = Stream.concat(stream,
                            IntStream.range(0, instance.numClasses())
                                    .mapToObj(aClass::value));
                    String lines = Stream.concat(stream, Stream.of("acc_mean")).collect(Collectors.joining(","));

                    Files.write(Paths.get(outputFile), (lines + System.lineSeparator()).getBytes(), StandardOpenOption.CREATE);
                    System.out.println(lines);
                }

                for (String classifier : classifiers) {
                    Classifier c = AbstractClassifier.forName(classifier, new String[]{});
                    c.buildClassifier(train);
                    Evaluation eval = new Evaluation(instance);
                    eval.evaluateModel(c, test);

                    Stream<String> stream = Stream.concat(Stream.of(name), Stream.of(classifier));
                    stream = Stream.concat(stream,
                            IntStream.range(0, instance.numClasses())
                                    .mapToDouble(eval::truePositiveRate)
                                    .mapToObj(BigDecimal::valueOf)
                                    .map(BigDecimal::toString));

                    String lines = Stream.concat(stream, Stream.of(BigDecimal.valueOf(eval.pctCorrect()).toString())).collect(Collectors.joining(","));
                    Files.write(Paths.get(outputFile), (lines + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                    System.out.println(lines);

                    saveConfusionMatrixToCSV(eval.toMatrixString(), name + "_" + c.getClass().getSimpleName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static void saveConfusionMatrixToCSV(String input, String filename) throws IOException {
        String s = Stream.of(input.split("\\n")).skip(3).map(Classify::asNumber).collect(Collectors.joining("\n"));
        String outputDir = ARFFS_FOLDER + File.separator + CONFUSION_MATRIX_OUTPUT_DIR;
        if (!Files.exists(Paths.get(outputDir))) {
            Files.createDirectory(Paths.get(outputDir));
        }
        Files.write(Paths.get(outputDir + File.separator + filename + ".csv"), s.getBytes(), StandardOpenOption.CREATE);
    }


    private static String asNumber(String s) {
        return mapRow(s, true);
    }

    private static String asPercentage(String s) {
        return mapRow(s, false);

    }

    private static String mapRow(String s, boolean asNumber) {
        String[] row = s.split("\\s+");
        BigDecimal sum = Stream.of(row).skip(1).limit(row.length - 5).map(BigDecimal::new).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return IntStream.range(1, row.length)
                .filter(i -> i <= row.length - 5 || i == row.length - 1)
                .mapToObj(i -> i <= row.length - 5 && !asNumber ? new BigDecimal(row[i]).divide(sum, 3, RoundingMode.HALF_UP).toString() : row[i])
                .collect(Collectors.joining(","));
    }

    public static void main(String[] args) throws Exception {
        ARFFS_FOLDER = "/mnt/sdb1/datasets/mammoset/exp5-2_aug";
        Classify.start(ARFFS_FOLDER, com.dhaalves.weka.model.Classifier.getWekaClasses());
    }

}
