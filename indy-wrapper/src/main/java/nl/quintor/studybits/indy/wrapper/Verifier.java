package nl.quintor.studybits.indy.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.EntitiesFromLedger;
import nl.quintor.studybits.indy.wrapper.dto.Proof;
import nl.quintor.studybits.indy.wrapper.dto.ProofAttribute;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.indy.wrapper.exception.IndyWrapperException;
import nl.quintor.studybits.indy.wrapper.util.IntegerEncodingUtil;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import org.apache.commons.lang3.Validate;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.quintor.studybits.indy.wrapper.util.AsyncUtil.wrapException;

@Slf4j
public class Verifier extends WalletOwner {
    public Verifier(String name, IndyPool pool, IndyWallet wallet) {
        super(name, pool, wallet);
    }

    public CompletableFuture<List<ProofAttribute>> getVerifiedProofAttributes(ProofRequest proofRequest, Proof proof) {
        return getEntitiesFromLedger(proof.getIdentifiers())
                .thenCompose(wrapException(entitiesFromLedger -> validateProof(proofRequest, proof, entitiesFromLedger)))
                .thenRun(() -> validateProofEncodings(proof))
                .thenApply(v -> extractProofAttributes(proofRequest, proof));
    }

    private void ValidateResult(boolean valid, String message) throws IndyWrapperException {
        if (!valid) {
            throw new IndyWrapperException(message);
        }
    }

    private CompletableFuture<Void> validateProof(ProofRequest proofRequest, Proof proof, EntitiesFromLedger entitiesFromLedger) throws JsonProcessingException, IndyException {
        String proofRequestJson = proofRequest.toJSON();
        String proofJson = proof.toJSON();
        String schemaJson = JSONUtil.mapper.writeValueAsString(entitiesFromLedger.getSchemas());
        String claimDefsJson = JSONUtil.mapper.writeValueAsString(entitiesFromLedger.getClaimDefs());
        String revocRegs = "{}";
        return Anoncreds
                .verifierVerifyProof(proofRequestJson, proofJson, schemaJson, claimDefsJson, revocRegs)
                .thenAccept(result -> ValidateResult(result, "Invalid proof: verifierVerifyProof failed."));
    }

    private void validateProofEncodings(Proof proof) {
        boolean valid = proof
                .getRequestedProof()
                .getRevealedAttributes()
                .entrySet()
                .stream()
                .filter(entry -> !IntegerEncodingUtil.validateProofEncoding(entry.getValue()))
                .peek(entry -> log.error("Wallet '{}': Invalid proof received from theirDid '{}', entry '{}'", getName(), proof.getTheirDid(), entry))
                .count() == 0;

        ValidateResult(valid, "Invalid proof: encodings invalid");
    }

    private List<ProofAttribute> extractProofAttributes(ProofRequest proofRequest, Proof proof) {
        Map<String, String> attributeNameLookup = proofRequest
                .getRequestedAttrs()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getName()));

        // Get both the self attested and the revealed attributes
        Stream<ProofAttribute> selfAttestedStream = proof.getRequestedProof()
                .getSelfAttestedAttributes()
                .entrySet()
                .stream()
                .map(entry -> new ProofAttribute(entry.getKey(), attributeNameLookup.get(entry.getKey()), entry.getValue()));

        Stream<ProofAttribute> revealedStream = proof.getRequestedProof()
                .getRevealedAttributes()
                .entrySet()
                .stream()
                .map(entry -> new ProofAttribute(entry.getKey(), attributeNameLookup.get(entry.getKey()), entry.getValue().get(1)));

        return Stream
                .concat(selfAttestedStream, revealedStream)
                .collect(Collectors.toList());
    }

}

