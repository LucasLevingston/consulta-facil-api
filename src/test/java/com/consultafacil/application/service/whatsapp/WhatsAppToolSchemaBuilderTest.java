package com.consultafacil.application.service.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppToolSchemaBuilderTest {

    private final ArrayNode tools = new WhatsAppToolSchemaBuilder(new ObjectMapper()).build();

    private JsonNode tool(String name) {
        for (JsonNode t : tools) {
            if (name.equals(t.path("name").asText())) return t;
        }
        throw new AssertionError("tool not found: " + name);
    }

    private List<String> required(String name) {
        List<String> out = new ArrayList<>();
        tool(name).path("input_schema").path("required").forEach(n -> out.add(n.asText()));
        return out;
    }

    @Test
    void build_exposesTheFourSchedulingTools() {
        List<String> names = new ArrayList<>();
        tools.forEach(t -> names.add(t.path("name").asText()));

        assertThat(names).containsExactly(
                "search_professionals", "list_appointments", "book_appointment", "cancel_appointment");
        tools.forEach(t -> {
            assertThat(t.path("description").asText()).isNotBlank();
            assertThat(t.path("input_schema").path("type").asText()).isEqualTo("object");
        });
    }

    @Test
    void build_declaresRequiredArgumentsPerTool() {
        assertThat(required("search_professionals")).isEmpty();
        assertThat(required("book_appointment")).containsExactly("professional_id", "date_time");
        assertThat(required("cancel_appointment")).containsExactly("appointment_id");
    }

    @Test
    void build_describesStringProperties() {
        JsonNode props = tool("book_appointment").path("input_schema").path("properties");

        assertThat(props.path("date_time").path("type").asText()).isEqualTo("string");
        assertThat(props.path("date_time").path("description").asText()).contains("yyyy-MM-ddTHH:mm");
        assertThat(props.has("reason")).isTrue();
        assertThat(tool("list_appointments").path("input_schema").path("properties").size()).isZero();
    }
}
