package com.katalon.license;


public class ComponentUtils {

  private static LicenseUtils licenseUtils;

//  public static <T> T getComponent(Class<T> clazz) {
//    return ComponentManager.getComponentInstanceOfType(clazz);
//  }

  public static LicenseUtils getLicenseUtils() {
    return licenseUtils;
  }

  public static void setLicenseUtils(LicenseUtils licenseUtils) {
    ComponentUtils.licenseUtils = licenseUtils;
  }
}
