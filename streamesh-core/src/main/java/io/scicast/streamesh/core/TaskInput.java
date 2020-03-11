package io.scicast.streamesh.core;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
public class TaskInput {

    private String baseCmd;
    private List<TaskParameter> parameters = new ArrayList<>();

}
