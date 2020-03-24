package io.scicast.streamesh.core.internal.reflect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class ScopeTest {


   @Test(expected = IllegalArgumentException.class)
   public void testScope() throws IOException {
       Scope scope = Scope.builder()
               .build();

       Scope child1 = Scope.builder()
               .build();

       Scope grandChild = Scope.builder().build();
       child1 = child1.attach(grandChild, Arrays.asList("child"), false);

       Scope child2 = Scope.builder()
               .build();


       scope = scope.attach(child1, Arrays.asList("child1"), false);
       scope = scope.attach(child2, Arrays.asList("child1"), false);

       new ObjectMapper().writer(SerializationFeature.INDENT_OUTPUT).writeValue(System.out, scope);
   }



}
