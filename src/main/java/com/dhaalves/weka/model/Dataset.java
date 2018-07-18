package com.dhaalves.weka.model;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum Dataset {
    SOYBEAN1("/home/daniel/Desktop/soybean1"),
    SOYBEAN2("/home/daniel/Desktop/soybean2"),
    SOYBEAN3("/home/daniel/Desktop/soybean3"),
    LEAVES1("/home/daniel/Desktop/leaves1"),
    PARASITES("/mnt/sda1/parasites");

    private String path;

    Dataset(String path) {
        this.path = path;
    }

    public Path getPath() {
        return Paths.get(path);
    }

    public static String getName(String filename){
        for (Dataset dataset : Dataset.values()) {
            if(filename.contains(dataset.name())){
                return dataset.name();
            }
        }
        return null;
    }
}
