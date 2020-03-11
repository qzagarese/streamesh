package io.scicast.streamesh.core.internal;

import io.scicast.streamesh.core.Definition;
import io.scicast.streamesh.core.Micropipe;
import io.scicast.streamesh.core.TaskDescriptor;
import io.scicast.streamesh.core.StreameshStore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemoryStreameshStore implements StreameshStore {

    private Map<String, Definition> definitions = new HashMap<>();
    private Map<String, Definition> definitionsByName = new HashMap<>();
    private Map<String, TaskDescriptor> jobs = new HashMap<String, TaskDescriptor>();
    private Map<Micropipe, Set<TaskDescriptor>> pipesToTasks = new HashMap<Micropipe, Set<TaskDescriptor>>();

    public InMemoryStreameshStore() {
    }

    @Override
    public void storeDefinition(Definition definition) {
        Definition previous = definitionsByName.get(definition.getName());
        if(previous != null) {
            definitions.remove(previous.getId());
        }
        definitions.put(definition.getId(), definition);
        definitionsByName.put(definition.getName(), definition);
    }


    @Override
    public Definition getDefinitionById(String id) {
        return definitions.get(id);
    }

    @Override
    public Definition getDefinitionByName(String name) {
        return definitionsByName.get(name);
    }

    @Override
    public void remove(String id) {
        Definition removed = definitions.remove(id);
        if(removed != null) {
            definitionsByName.remove(removed.getName());
        }
    }

    @Override
    public Set<Definition> getAllDefinitions() {
        return definitions.entrySet().stream()
                .map(e -> e.getValue())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<TaskDescriptor> getAllJobs() {
        return jobs.values().stream().collect(Collectors.toSet());
    }

    @Override
    public Set<TaskDescriptor> getJobsByDefinition(String definitionId) {
        return pipesToTasks.get(getDefinitionById(definitionId));
    }

    @Override
    public TaskDescriptor getJobById(String jobId) {
        return jobs.get(jobId);
    }

    @Override
    public void updateJob(String definitionId, TaskDescriptor descriptor) {
        jobs.put(descriptor.getId(), descriptor);
        Definition definition = getDefinitionById(definitionId);
        if (!(definition instanceof  Micropipe)) {
            throw new IllegalArgumentException("Cannot associate job to definition of type " + definition.getType());
        }
        Set<TaskDescriptor> jobDescriptors = pipesToTasks.get(definition);
        if (jobDescriptors == null) {
            jobDescriptors = new HashSet<>();
        } else {
            jobDescriptors.remove(descriptor);
            jobDescriptors.add(descriptor);
        }
        pipesToTasks.put((Micropipe) definition, jobDescriptors);
    }
}