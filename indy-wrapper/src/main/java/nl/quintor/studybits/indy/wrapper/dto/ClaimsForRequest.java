package nl.quintor.studybits.indy.wrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ClaimsForRequest implements Serializable {
    private Map<String, List<ClaimReferent>> attrs;
    private Map<String, List<ClaimReferent>> predicates;
}
