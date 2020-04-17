package io.scicast.streamesh.core;

import io.scicast.streamesh.core.crypto.CryptoUtil;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@With
@EqualsAndHashCode(of = "id")
public class TaskDescriptor {

    private String id;
    private TaskStatus status;
    private String serviceName;
    private String serviceId;
    private String containerId;
    private String errorMessage;
    private CryptoUtil.WrappedAesGCMKey key;
    private LocalDateTime started;
    private LocalDateTime exited;

    public enum TaskStatus {
        FAILED, RUNNING, COMPLETE, KILLED
    }

}
