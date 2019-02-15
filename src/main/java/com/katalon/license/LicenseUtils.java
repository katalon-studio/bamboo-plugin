package com.katalon.license;

import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import org.osgi.service.component.annotations.Component;

@Component
public class LicenseUtils {

  private PluginLicenseManager pluginLicenseManager;

  public LicenseUtils(PluginLicenseManager pluginLicenseManager) {
    ComponentUtils.setLicenseUtils(this);
    this.pluginLicenseManager = pluginLicenseManager;
  }

  public LicenseValidation validateLicense() {
    LicenseValidation licenseValidation = new LicenseValidation();
    boolean valid;
    try {
      Option<PluginLicense> licenseOption = pluginLicenseManager.getLicense();
      if (licenseOption.isDefined()) {
        PluginLicense license = licenseOption.get();
        valid = license != null && license.isValid();
      } else {
        valid = false;
      }
    } catch (Exception e) {
      valid = false;
    }
    licenseValidation.setValid(valid);
    return licenseValidation;
  }
}
