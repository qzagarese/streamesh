package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;

@Getter
@Builder
@With
@EqualsAndHashCode(of = "id")
public class JobDescriptor {

    private String id;
    private JobStatus status;
    private String containerId;
    private String errorMessage;

    public enum JobStatus {
        FAILED, RUNNING, COMPLETE
    }

}
