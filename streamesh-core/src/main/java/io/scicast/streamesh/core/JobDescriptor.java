package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Getter
@Builder
@With
public class JobDescriptor {

    private String id;
    private JobStatus status;
    private String containerId;

    enum JobStatus {
        FAILED, RUNNING, COMPLETE
    }

}
