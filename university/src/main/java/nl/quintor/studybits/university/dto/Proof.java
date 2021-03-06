package nl.quintor.studybits.university.dto;

import java.util.List;

public interface Proof extends Versioned {

    default List<ProofAttribute> getProofAttributes() {
        return ClaimUtils.getProofAttributes(this.getClass());
    }

}
