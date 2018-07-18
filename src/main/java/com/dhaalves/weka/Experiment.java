package com.dhaalves.weka;


import com.dhaalves.weka.model.Classifier;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Experiment {

    public static void main(String[] args) throws Exception {

        if (args.length == 1) {
            String ARFF_FOLDER = args[0];

            String PARAMS = "-runs 10 -splittype randomsplit -percentage 80.0 ";

            for (Classifier classifier : Classifier.values()) {
                Files.list(Paths.get(ARFF_FOLDER)).forEach(path -> {
                    String p = "-c " + classifier.getWekaClass() + " -d " + path.toString();
                    try {
                        Experimenter.run((PARAMS + p).split(" "));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            }
        } else {
            System.out.println("Execute: java -jar <caminho_arffs>");
        }

    }
}
