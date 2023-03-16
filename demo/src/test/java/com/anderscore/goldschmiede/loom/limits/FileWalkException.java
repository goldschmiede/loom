package com.anderscore.goldschmiede.loom.limits;

import java.nio.file.Path;

public class FileWalkException extends RuntimeException {

    public FileWalkException(Path path, Exception ex) {
        super("Cannot determine size of " + path, ex);
    }
}
