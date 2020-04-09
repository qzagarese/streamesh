package io.scicast.streamesh.core.internal.reflect;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.scicast.streamesh.core.MicroPipe;
import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.StreameshStore;
import io.scicast.streamesh.core.exception.InvalidCmdParameterException;
import io.scicast.streamesh.core.exception.MissingParameterException;
import io.scicast.streamesh.core.flow.FlowDefinition;
import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowGraphBuilder;
import io.scicast.streamesh.core.flow.FlowParameter;
import io.scicast.streamesh.core.flow.execution.ExecutablePipeRuntimeNode;
import io.scicast.streamesh.core.flow.execution.ExecutionGraph;
import io.scicast.streamesh.core.flow.execution.FlowParameterRuntimeNode;
import io.scicast.streamesh.core.flow.execution.RuntimeDataValue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScopeFactoryTest {

    public static final String MERGER_NAME = "http-data-merger";
    public static final String PLOTTER_NAME = "python-plotter";
    public static final String DOWNLOADER_NAME = "s3-downloader";
    public static final String DB_READER_NAME = "simple-db-reader";
    private static final String MICROPIPES_PATH = "/micropipes/";

    private static StreameshStore streameshStore;
    private static MicroPipe merger;
    private static MicroPipe plotter;
    private static MicroPipe s3Downloader;
    private static MicroPipe dbReader;
    private static ObjectMapper mapper = new YAMLMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private StreameshContext context;

    private ObjectMapper jsonMapper = new ObjectMapper();

    @BeforeClass
    public static void setUpClass() throws IOException {
        merger = loadDefinition(MICROPIPES_PATH + "http-data-merger.yml", MicroPipe.class).withId(UUID.randomUUID().toString());
        plotter = loadDefinition(MICROPIPES_PATH + "python-plotter.yml", MicroPipe.class).withId(UUID.randomUUID().toString());
        s3Downloader = loadDefinition(MICROPIPES_PATH + "s3-downloader.yml", MicroPipe.class).withId(UUID.randomUUID().toString());
        dbReader = loadDefinition(MICROPIPES_PATH + "simple-db-reader.yml", MicroPipe.class).withId(UUID.randomUUID().toString());

        streameshStore = mock(StreameshStore.class);
        when(streameshStore.getDefinitionByName(MERGER_NAME)).thenReturn(merger);
        when(streameshStore.getDefinitionByName(PLOTTER_NAME)).thenReturn(plotter);
        when(streameshStore.getDefinitionByName(DOWNLOADER_NAME)).thenReturn(s3Downloader);
        when(streameshStore.getDefinitionByName(DB_READER_NAME)).thenReturn(dbReader);
    }

    @Before
    public void setUp() {
        context = StreameshContext.builder()
                .store(streameshStore)
                .build();
    }

    @Test
    public void testScopeCreation() throws IOException {
        FlowDefinition definition = loadDefinition("/flows/airbnb-flow.yml", FlowDefinition.class);
        ScopeFactory factory = ScopeFactory.builder()
                .streameshContext(context)
                .build();
        Scope scope = factory.create(definition);
        FlowGraph graph = new FlowGraphBuilder().build(scope);

        ExecutionGraph executionGraph = new ExecutionGraph(graph);

        Set<ExecutablePipeRuntimeNode> executableNodes = executionGraph.getExecutableNodes();


        Map<String, Object> input = new HashMap<>();
        input.put("manhattan-bucket", "ic-demo-streamesh");
        input.put("manhattan-file", "data/AB_NYC_2019_Manhattan.csv");
        input.put("others-bucket", "ic-demo-streamesh");
        input.put("others-file", "data/AB_NYC_2019_rest.csv");

        executionGraph.getPipeInputNodes().forEach(node -> {
            if (node.isStaticallyInitialised()) {
                node.notifyObservers();
            }
        });

        Set<FlowParameterRuntimeNode> inputNodes = executionGraph.getInputNodes();
        inputNodes.forEach(node -> {
            FlowParameter parameterSpec = (FlowParameter) node.getStaticGraphNode().getValue();
            Object o = input.get(parameterSpec.getName());
            node.update(buildRuntimeDataValue(parameterSpec, o));
        });

        executableNodes = executionGraph.getExecutableNodes();

        System.out.println(graph.toDot());

//        graph.getNodes().stream()
//                .forEach(System.out::println);

//        explainScope(scope, new ArrayList<>());

//        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT)
//                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
//                .writerFor(Scope.class).writeValue(System.out, scope);

    }

    @Test
    public void testScopeForSubFlow() throws IOException {
        FlowDefinition airbnb = loadDefinition("/flows/airbnb-flow.yml", FlowDefinition.class);
        when(streameshStore.getDefinitionByName("airbnb-ny-properties")).thenReturn(airbnb);

        FlowDefinition definition = loadDefinition("/flows/recursive-airbnb-flow.yml", FlowDefinition.class);
        ScopeFactory factory = ScopeFactory.builder()
                .streameshContext(context)
                .build();

        Scope scope = factory.create(definition);
        FlowGraph graph = new FlowGraphBuilder().build(scope);
        System.out.println(graph.toDot());


//        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT)
//                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
//                .writerFor(Scope.class).writeValue(System.out, scope);


    }

    private void explainScope(Scope currentScope, List<String> basePath) {
        String stringifiedBasePath = basePath.stream().collect(Collectors.joining("/"));
        Object value = currentScope.getValue();
        if (value == null) {
            System.out.println("/" + stringifiedBasePath + " is null.");
        } else if (value instanceof  String) {
            System.out.println("/" + stringifiedBasePath + " is " + value.toString());
        } else {
            System.out.println("/" + stringifiedBasePath + " is a " + value.getClass().getSimpleName() + "[" + value.hashCode() + "]");
        }

        currentScope.getStructure().entrySet().forEach(entry -> {
            explainScope(entry.getValue(),
                    Stream.concat(basePath.stream(), Stream.of(entry.getKey()))
                            .collect(Collectors.toList()));
        });
    }

    private static <T> T loadDefinition(String resource, Class<T> clazz) throws IOException {
        return mapper.reader().forType(clazz).readValue(ScopeFactoryTest.class.getResource(resource));
    }

    private RuntimeDataValue buildRuntimeDataValue(FlowParameter parameterSpec, Object o) {
        if (!parameterSpec.isOptional() && o == null) {
            throw new MissingParameterException(String.format("Parameter %s is mandatory.", parameterSpec.getName()));
        }
        if (parameterSpec.isRepeatable() && (!List.class.isAssignableFrom(o.getClass()))) {
            throw new InvalidCmdParameterException(String.format("Parameter %s must be provided as an array", parameterSpec.getName()));
        }

        RuntimeDataValue.RuntimeDataValueBuilder builder = RuntimeDataValue.builder();
        Set<RuntimeDataValue.RuntimeDataValuePart> parts;
        if (!parameterSpec.isRepeatable()) {
            parts = Stream.of(RuntimeDataValue.RuntimeDataValuePart.builder()
                    .state(RuntimeDataValue.DataState.COMPLETE)
                    .refName(parameterSpec.getName())
                    .value((String) o)
                    .build())
                    .collect(Collectors.toSet());
        } else {
            parts = ((List<String>) o).stream()
                    .map(s -> RuntimeDataValue.RuntimeDataValuePart.builder()
                            .value(s)
                            .refName(parameterSpec.getName())
                            .state(RuntimeDataValue.DataState.COMPLETE)
                            .build())
                    .collect(Collectors.toSet());
        }
        return builder.parts(parts)
                .build();
    }

}
