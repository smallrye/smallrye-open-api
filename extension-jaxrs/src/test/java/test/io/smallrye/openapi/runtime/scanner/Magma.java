package test.io.smallrye.openapi.runtime.scanner;

import java.util.UUID;

public class Magma extends BaseModel {

    String codename;
    String tier;
    KingCrimson deployment;

    public Magma() {
    }

    public Magma(UUID id) {
        super(id);
    }

    public String getCodename() {
        return codename;
    }

    public void setCodename(String codename) {
        this.codename = codename;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public KingCrimson getDeployment() {
        return deployment;
    }

    public void setDeployment(KingCrimson deployment) {
        this.deployment = deployment;
    }

}
