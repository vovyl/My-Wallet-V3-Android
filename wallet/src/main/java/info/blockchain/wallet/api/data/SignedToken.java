package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

public class SignedToken {

    private boolean success;
    @Nullable
    private String token;
    @Nullable
    private String error;

    @JsonCreator
    public SignedToken(@JsonProperty("success") boolean success,
                       @JsonProperty("token") @Nullable String token,
                       @JsonProperty("error") @Nullable String error) {
        this.success = success;
        this.token = token;
        this.error = error;
    }

    public boolean isSuccessful() {
        return success;
    }

    @Nullable
    public String getToken() {
        return token;
    }

    @Nullable
    public String getError() {
        return error;
    }
}