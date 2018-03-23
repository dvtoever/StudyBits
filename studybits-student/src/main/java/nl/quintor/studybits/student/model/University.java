package nl.quintor.studybits.student.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@AllArgsConstructor
public class University {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String endpoint;
}