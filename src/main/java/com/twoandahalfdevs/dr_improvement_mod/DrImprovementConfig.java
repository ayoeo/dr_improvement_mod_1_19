package com.twoandahalfdevs.dr_improvement_mod;

//@ConfigSerializable
public class DrImprovementConfig {
  public static final DrImprovementConfig DEFAULT = new DrImprovementConfig();

  private boolean disableWTapSprint = true;

  public DrImprovementConfig() {
  }

  public DrImprovementConfig(boolean disableWTapSprint) {
    this.disableWTapSprint = disableWTapSprint;
  }

  public boolean shouldDisableWTapSprint() {
    return disableWTapSprint;
  }
}