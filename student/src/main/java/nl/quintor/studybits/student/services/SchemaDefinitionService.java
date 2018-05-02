package nl.quintor.studybits.student.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.student.entities.SchemaDefinitionRecord;
import nl.quintor.studybits.student.repositories.SchemaDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SchemaDefinitionService {

    private final SchemaDefinitionRepository schemaDefinitionRepository;

    @Transactional
    public SchemaDefinitionRecord getOrSave(SchemaDefinitionRecord record) {
        return schemaDefinitionRepository
                .findByNameIgnoreCaseAndVersion(record.getName(), record.getVersion())
                .orElseGet(() -> schemaDefinitionRepository.save(record));
    }
}