package ext.vnua.veterinary_beapp.modules.audits.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class AuditComparisonUtil {

    private final ObjectMapper objectMapper;

    public AuditComparisonUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<String> getChangedFields(String oldJson, String newJson) {
        List<String> changes = new ArrayList<>();

        try {
            JsonNode oldNode = objectMapper.readTree(oldJson);
            JsonNode newNode = objectMapper.readTree(newJson);

            compareNodes("", oldNode, newNode, changes);

        } catch (Exception e) {
            changes.add("Error comparing values: " + e.getMessage());
        }

        return changes;
    }

    private void compareNodes(String prefix, JsonNode oldNode, JsonNode newNode, List<String> changes) {
        if (oldNode == null && newNode == null) return;

        if (oldNode == null) {
            changes.add(prefix + " was added");
            return;
        }

        if (newNode == null) {
            changes.add(prefix + " was removed");
            return;
        }

        if (oldNode.isObject() && newNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = oldNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                String currentPath = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;

                JsonNode oldValue = field.getValue();
                JsonNode newValue = newNode.get(fieldName);

                compareNodes(currentPath, oldValue, newValue, changes);
            }

            // Check for new fields
            Iterator<Map.Entry<String, JsonNode>> newFields = newNode.fields();
            while (newFields.hasNext()) {
                Map.Entry<String, JsonNode> field = newFields.next();
                if (!oldNode.has(field.getKey())) {
                    String currentPath = prefix.isEmpty() ? field.getKey() : prefix + "." + field.getKey();
                    changes.add(currentPath + " was added: " + field.getValue().toString());
                }
            }

        } else if (!oldNode.equals(newNode)) {
            changes.add(prefix + " changed from '" + oldNode.asText() + "' to '" + newNode.asText() + "'");
        }
    }
}
