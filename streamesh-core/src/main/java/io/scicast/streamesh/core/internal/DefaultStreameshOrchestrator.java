package io.scicast.streamesh.core.internal;

import io.scicast.streamesh.core.*;
import io.scicast.streamesh.core.crypto.CryptoUtil;
import io.scicast.streamesh.core.exception.InvalidCmdParameterException;
import io.scicast.streamesh.core.exception.MissingParameterException;
import io.scicast.streamesh.core.exception.NotFoundException;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultStreameshOrchestrator implements StreameshOrchestrator {

    private final StreameshStore streameshStore = new InMemoryStreameshStore();
    private Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    private OrchestrationDriver driver;

    public DefaultStreameshOrchestrator(String serverIpAddress) {
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
        this.driver.setStreameshServerAddress(serverIpAddress);
    }

    public String applyDefinition(Micropipe definition) {

        String imageId = driver.retrieveContainerImage(definition.getImage());
        String definitionId = UUID.randomUUID().toString();

        Micropipe callableDefinition = definition.withImageId(imageId)
                .withId(definitionId);
        streameshStore.storeDefinition(definition);
        return definitionId;
    }

    public Micropipe getDefinition(String id) {
        Micropipe definition = streameshStore.getDefinitionById(id);
        if(definition == null) {
            throw new NotFoundException(String.format("No definition with id %s found", id));
        }
        return definition;
    }

    public Micropipe getDefinitionByName(String name) {
        Micropipe definition = streameshStore.getDefinitionByName(name);
        if(definition == null) {
            throw new NotFoundException(String.format("No definition found for name %s", name));
        }
        return definition;
    }

    public void removeDefinition(String id) {
        streameshStore.remove(id);
    }

    public Set<Micropipe> getDefinitions() {
        return streameshStore.getAllDefinitions();
    }

    public Set<JobDescriptor> getAllJobs() {
        return streameshStore.getAllJobs();
    }

    public Set<JobDescriptor> getJobsByDefinition(String definitionId) {
        return streameshStore.getJobsByDefinition(definitionId);
    }

    public JobDescriptor scheduleJob(String definitionId, Map<?, ?> input) {
        Micropipe definition = getDefinition(definitionId);
        JobDescriptor descriptor = driver.scheduleJob(definition.getImage(),
                buildCommand(definition, input),
                definition.getOutputMapping(),
                desc -> updateIndexes(definition, desc));
        updateIndexes(definition, descriptor);
        return descriptor;
    }

    public JobDescriptor scheduleSecureJob(String definitionId, Map<?, ?> input, String publicKey) {
        CryptoUtil.WrappedAesGCMKey wrappedKey = CryptoUtil.createWrappedKey(publicKey);
        JobDescriptor descriptor = scheduleJob(definitionId, input);
        descriptor.setKey(wrappedKey);
        return descriptor;
    }

    public JobDescriptor getJob(String jobId) {
        JobDescriptor job = streameshStore.getJobById(jobId);
        if (job == null) {
            throw new NotFoundException(String.format("No job found for id %s", jobId));
        }
        return job;
    }

    private void updateIndexes(Micropipe definition, JobDescriptor descriptor) {
        streameshStore.updateJob(definition.getId(), descriptor);
    }

    private String buildCommand(Micropipe definition, Map<?, ?> input) {
        InputMapping inputMapping = definition.getInputMapping();
        String params = inputMapping.getParameters().stream()
                .map(p -> {
                    Object o = input.get(p.getName());
                    if (!p.isOptional() && o == null) {
                        throw new MissingParameterException(String.format("Parameter %s is mandatory.", p.getName()));
                    }
                    if (p.isRepeatable() && (!List.class.isAssignableFrom(o.getClass()))) {
                        throw new InvalidCmdParameterException(String.format("Parameter %s must be provided as an array", p.getName()));
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
        InputStream stream = driver.getJobOutput(jobDescriptorId);
        if (job.getKey() != null) {
            stream = CryptoUtil.getCipherInputStream(stream, job.getKey());
        }
        return stream;
    }
}
