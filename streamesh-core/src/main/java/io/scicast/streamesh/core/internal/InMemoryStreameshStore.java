package io.scicast.streamesh.core.internal;

import io.scicast.streamesh.core.Micropipe;
import io.scicast.streamesh.core.JobDescriptor;
import io.scicast.streamesh.core.StreameshStore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemoryStreameshStore implements StreameshStore {

    private Map<String, Micropipe> definitions = new HashMap<String, Micropipe>();
    private Map<String, Micropipe> definitionsByName = new HashMap<String, Micropipe>();
    private Map<String, JobDescriptor> jobs = new HashMap<String, JobDescriptor>();
    private Map<Micropipe, Set<JobDescriptor>> definitionToJobs = new HashMap<Micropipe, Set<JobDescriptor>>();

    public InMemoryStreameshStore() {
    }

    @Override
    public void storeDefinition(Micropipe definition) {
        Micropipe previous = definitionsByName.get(definition.getName());
        if(previous != null) {
            definitions.remove(previous);
        }
        definitions.put(definition.getId(), definition);
        definitionsByName.put(definition.getName(), definition);
    }


    @Override
    public Micropipe getDefinitionById(String id) {
        return definitions.get(id);
    }

    @Override
    public Micropipe getDefinitionByName(String name) {
        return definitionsByName.get(name);
    }

    @Override
    public void remove(String id) {
        Micropipe removed = definitions.remove(id);
        if(removed != null) {
            definitionsByName.remove(removed.getName());
        }
    }

    @Override
    public Set<Micropipe> getAllDefinitions() {
        return definitions.entrySet().stream()
                .map(e -> e.getValue())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<JobDescriptor> getAllJobs() {
        return jobs.values().stream().collect(Collectors.toSet());
    }

    @Override
    public Set<JobDescriptor> getJobsByDefinition(String definitionId) {
        return definitionToJobs.get(getDefinitionById(definitionId));
    }

    @Override
    public JobDescriptor getJobById(String jobId) {
        return jobs.get(jobId);
    }

    @Override
    public void updateJob(String definitionId, JobDescriptor descriptor) {
        jobs.put(descriptor.getId(), descriptor);
        Micropipe definition = getDefinitionById(definitionId);
        Set<JobDescriptor> jobDescriptors = definitionToJobs.get(definition);
        if (jobDescriptors == null) {
            jobDescriptors = new HashSet<>();
        } else {
            jobDescriptors.remove(descriptor);
            jobDescriptors.add(descriptor);
        }
        definitionToJobs.put(definition, jobDescriptors);
    }
}