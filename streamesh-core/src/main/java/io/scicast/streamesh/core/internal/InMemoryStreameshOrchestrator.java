package io.scicast.streamesh.core.internal;

import io.scicast.streamesh.core.*;
import io.scicast.streamesh.core.exception.InvalidCmdParameterException;
import io.scicast.streamesh.core.exception.MissingParameterException;
import io.scicast.streamesh.core.exception.NotFoundException;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class InMemoryStreameshOrchestrator implements StreameshOrchestrator {

    private Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    private OrchestrationDriver driver;
    private Map<String, CallableDefinition> definitions = new HashMap<>();
    private Map<String, CallableDefinition> definitionsByName = new HashMap<>();
    private Map<String, JobDescriptor> jobs = new HashMap<>();
    private Map<CallableDefinition, Set<JobDescriptor>> definitionToJobs = new HashMap<>();

    public InMemoryStreameshOrchestrator() {
        ServiceLoader<OrchestrationDriver> loader = ServiceLoader.load(OrchestrationDriver.class);

        this.driver = StreamSupport.stream(loader.spliterator(), false)
                .peek(impl -> logger.info(
                        "Found orchestration driver of type " + impl.getClass().getSimpleName()))
                .findFirst()
                .map(impl -> {
                    logger.info("Using orchestration driver " + impl.getClass().getSimpleName());
                    return impl;
                })
                .orElseThrow(() -> new RuntimeException("No orchestration driver. Booting sequence aborted."));
    }

    public String applyDefinition(CallableDefinition definition) {

        CallableDefinition referencedDefinition = definitionsByName.get(definition.getName());
        if(referencedDefinition != null) {
            removeDefinition(referencedDefinition.getId());
        }

        String imageId = driver.retrieveContainerImage(definition.getImage());
        String definitionId = UUID.randomUUID().toString();

        CallableDefinition callableDefinition = definition.withImageId(imageId)
                .withId(definitionId);
        definitions.put(definitionId, callableDefinition);
        definitionsByName.put(definition.getName(),callableDefinition);
        return definitionId;
    }

    public CallableDefinition getDefinition(String id) {
        CallableDefinition definition = definitions.get(id);
        if(definition == null) {
            throw new NotFoundException(String.format("No definition with id %s found", id));
        }
        return definition;
    }

    public CallableDefinition getDefinitionByName(String name) {
        CallableDefinition definition = definitionsByName.get(name);
        if(definition == null) {
            throw new NotFoundException(String.format("No definition found for name %s", name));
        }
        return definition;
    }

    public void removeDefinition(String id) {
        CallableDefinition removed = definitions.remove(id);
        if(removed != null) {
            definitionsByName.remove(removed.getName());
        }
    }

    public Set<CallableDefinition> getDefinitions() {
        return definitions.entrySet().stream()
                .map(e -> e.getValue())
                .collect(Collectors.toSet());
    }

    public Set<JobDescriptor> getAllJobs() {
        return jobs.values().stream().collect(Collectors.toSet());
    }

    public Set<JobDescriptor> getJobsByDefinition(String definitionId) {
        return definitionToJobs.get(getDefinition(definitionId));
    }

    public JobDescriptor scheduleJob(String definitionId, Map<?, ?> input) {
        CallableDefinition definition = getDefinition(definitionId);
        JobDescriptor descriptor = driver.scheduleJob(definition.getImage(),
                buildCommand(definition, input),
                definition.getOutputMapping(),
                desc -> updateIndexes(definition, desc));
        updateIndexes(definition, descriptor);
        return descriptor;
    }

    public JobDescriptor getJob(String jobId) {
        JobDescriptor job = jobs.get(jobId);
        if (job == null) {
            throw new NotFoundException(String.format("No job found for id %s", jobId));
        }
        return job;
    }

    private void updateIndexes(CallableDefinition definition, JobDescriptor descriptor) {
        jobs.put(descriptor.getId(), descriptor);
        Set<JobDescriptor> jobDescriptors = definitionToJobs.get(definition);
        if (jobDescriptors == null) {
            jobDescriptors = new HashSet<>();
        } else {
            jobDescriptors.remove(descriptor);
            jobDescriptors.add(descriptor);
        }
        definitionToJobs.put(definition, jobDescriptors);
    }

    private String buildCommand(CallableDefinition definition, Map<?, ?> input) {
        InputMapping inputMapping = definition.getInputMapping();
        String params = inputMapping.getParameters().stream()
                .map(p -> {
                    Object o = input.get(p.getExternalName());
                    if (!p.isOptional() && o == null) {
                        throw new MissingParameterException(String.format("Parameter %s is mandatory.", p.getExternalName()));
                    }
                    if (p.isRepeatable() && (!List.class.isAssignableFrom(o.getClass()))) {
                        throw new InvalidCmdParameterException(String.format("Parameter %s must be provided as an array", p.getExternalName()));
                    }
                    if (!p.isRepeatable()) {
                        String value = (String) o;
                        return p.getInternalName().trim() + " " + value.trim();
                    } else {
                        List<String> value = (List<String>) o;
                        return value.stream()
                                .map(v -> p.getInternalName().trim() + " " + v.trim())
                                .collect(Collectors.joining(" "));
                    }
                }).collect(Collectors.joining(" "));
        return (inputMapping.getBaseCmd().trim() + " " + params.trim()).trim();
    }

    public InputStream getJobOutput(String jobDescriptorId) {
        JobDescriptor job = getJob(jobDescriptorId);
        return driver.getJobOutput(jobDescriptorId);
    }
}
