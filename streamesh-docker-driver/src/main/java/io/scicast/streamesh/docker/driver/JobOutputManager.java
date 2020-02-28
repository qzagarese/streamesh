package io.scicast.streamesh.docker.driver;

import io.scicast.streamesh.docker.driver.internal.TailingInputStream;
import lombok.Getter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JobOutputManager {

    @Getter
    private final String outputFilePath;
    private final List<TailingInputStream> registeredTailers = new ArrayList<>();

    @Getter
    private final String outputName;
    private boolean jobTerminated = false;

    public JobOutputManager(String outputName, String outputFilePath) {
        this.outputName = outputName;
        this.outputFilePath = outputFilePath;
    }

    public InputStream requestStream() {
        TailingInputStream stream = new TailingInputStream(outputFilePath, jobTerminated);
        registeredTailers.add(stream);
        return stream;
    }

    public void notifyTermination() {
        this.jobTerminated = true;
        registeredTailers.forEach(tailer -> tailer.notifyWriteCompletion());
    }

}
