package io.scicast.streamesh.core.internal.reflect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.scicast.streamesh.core.Micropipe;
import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.StreameshStore;
import static org.mockito.Mockito.*;

import io.scicast.streamesh.core.flow.FlowDefinition;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class ScopeFactoryTest {

    public static final String MERGER_NAME = "http-data-merger";
    public static final String PLOTTER_NAME = "python-plotter";
    public static final String DOWNLOADER_NAME = "s3-downloader";
    public static final String DB_READER_NAME = "simple-db-reader";
    private static final String MICROPIPES_PATH = "/micropipes/";

    private static StreameshStore streameshStore;
    private static Micropipe merger;
    private static Micropipe plotter;
    private static Micropipe s3Downloader;
    private static Micropipe dbReader;
    private static ObjectMapper mapper = new YAMLMapper();
    private StreameshContext context;


    @BeforeClass
    public static void setUpClass() throws IOException {
        merger = loadDefinition(MICROPIPES_PATH + "http-data-merger.yml", Micropipe.class);
        plotter = loadDefinition(MICROPIPES_PATH + "python-plotter.yml", Micropipe.class);
        s3Downloader = loadDefinition(MICROPIPES_PATH + "s3-downloader.yml", Micropipe.class);
        dbReader = loadDefinition(MICROPIPES_PATH + "simple-db-reader.yml", Micropipe.class);

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
                .context(context)
                .build();
        Scope scope = factory.create(definition);

    }

    private static <T> T loadDefinition(String resource, Class<T> clazz) throws IOException {
        return mapper.reader().forType(clazz).readValue(ScopeFactoryTest.class.getResource(resource));
    }

}
