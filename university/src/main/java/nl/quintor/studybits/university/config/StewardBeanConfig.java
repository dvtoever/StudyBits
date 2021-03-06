package nl.quintor.studybits.university.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.TrustAnchor;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.wallet.WalletExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;

@Configuration
public class StewardBeanConfig {

    private final IndyPool indyPool;

    @Value("${steward.active}")
    private String stewardActive;

    @Value("${steward.walletname}")
    private String stewardWalletName;

    @Value("${steward.walletseed}")
    private String stewardWalletSeed;

    @Autowired
    public StewardBeanConfig(IndyPool indyPool) {
        this.indyPool = indyPool;
    }

    @Bean("stewardTrustAnchor")
    public TrustAnchor stewardTrustAnchor() throws Exception {
        IndyWallet stewardWallet = getIndyWallet(stewardWalletName, stewardWalletSeed);
        return new TrustAnchor("Steward", indyPool, stewardWallet);
    }

    private IndyWallet getIndyWallet(String name, String seed) throws IndyException, ExecutionException, InterruptedException, JsonProcessingException {
        try {
            return IndyWallet.create(indyPool, name, seed);
        } catch (Exception ex) {
            if (!(ex.getCause() instanceof WalletExistsException)) {
                throw ex;
            }
        }
        return new IndyWallet(name);
    }
}