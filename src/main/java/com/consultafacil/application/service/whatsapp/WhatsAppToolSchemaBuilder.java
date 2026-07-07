package com.consultafacil.application.service.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WhatsAppToolSchemaBuilder {

    private final ObjectMapper objectMapper;

    public ArrayNode build() {
        ArrayNode tools = objectMapper.createArrayNode();
        tools.add(searchProfessionalsTool());
        tools.add(listAppointmentsTool());
        tools.add(bookAppointmentTool());
        tools.add(cancelAppointmentTool());
        return tools;
    }

    private ObjectNode searchProfessionalsTool() {
        ObjectNode props = objectMapper.createObjectNode();
        props.set("specialty", stringProp("Especialidade médica"));
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.set("properties", props);
        schema.set("required", objectMapper.createArrayNode());
        return tool("search_professionals", "Busca profissionais por especialidade", schema);
    }

    private ObjectNode listAppointmentsTool() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.set("properties", objectMapper.createObjectNode());
        return tool("list_appointments", "Lista consultas do paciente", schema);
    }

    private ObjectNode bookAppointmentTool() {
        ObjectNode props = objectMapper.createObjectNode();
        props.set("professional_id", stringProp("ID do profissional"));
        props.set("date_time", stringProp("Data e hora: yyyy-MM-ddTHH:mm"));
        props.set("reason", stringProp("Motivo da consulta"));
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.set("properties", props);
        schema.set("required", objectMapper.createArrayNode().add("professional_id").add("date_time"));
        return tool("book_appointment", "Agenda uma consulta para o paciente", schema);
    }

    private ObjectNode cancelAppointmentTool() {
        ObjectNode props = objectMapper.createObjectNode();
        props.set("appointment_id", stringProp("ID da consulta"));
        props.set("reason", stringProp("Motivo do cancelamento"));
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.set("properties", props);
        schema.set("required", objectMapper.createArrayNode().add("appointment_id"));
        return tool("cancel_appointment", "Cancela uma consulta do paciente", schema);
    }

    private ObjectNode stringProp(String description) {
        ObjectNode prop = objectMapper.createObjectNode();
        prop.put("type", "string");
        prop.put("description", description);
        return prop;
    }

    private ObjectNode tool(String name, String description, ObjectNode inputSchema) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("name", name);
        tool.put("description", description);
        tool.set("input_schema", inputSchema);
        return tool;
    }
}
