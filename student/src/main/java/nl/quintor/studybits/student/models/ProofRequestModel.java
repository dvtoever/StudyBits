package nl.quintor.studybits.student.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProofRequestModel {
    private Long id;

    private Long proofId;

    private String studentUserName;

    private String universityName;

    private String link;

    private String name;

    private String version;
}