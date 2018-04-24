package piuk.blockchain.androidbuysell.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ExchangeData {

    public ExchangeData() {
        // Empty constructor
    }

    @JsonProperty("coinify")
    private CoinifyData coinify = null;

    @JsonProperty("sfox")
    private SfoxData sfox = null;

    @JsonProperty("unocoin")
    private UnocoinData unocoin = null;

    public CoinifyData getCoinify() {
        return coinify;
    }

    public void setCoinify(CoinifyData coinify) {
        this.coinify = coinify;
    }

    public SfoxData getSfox() {
        return sfox;
    }

    public void setSfox(SfoxData sfox) {
        this.sfox = sfox;
    }

    public UnocoinData getUnocoin() {
        return unocoin;
    }

    public void setUnocoin(UnocoinData unocoin) {
        this.unocoin = unocoin;
    }
}
