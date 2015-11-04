/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.5
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package nl.stcorp.coda;

public enum FormatEnum {
  coda_format_ascii,
  coda_format_binary,
  coda_format_xml,
  coda_format_hdf4,
  coda_format_hdf5,
  coda_format_cdf,
  coda_format_netcdf,
  coda_format_grib1,
  coda_format_grib2,
  coda_format_rinex,
  coda_format_sp3;

  public final int swigValue() {
    return swigValue;
  }

  public static FormatEnum swigToEnum(int swigValue) {
    FormatEnum[] swigValues = FormatEnum.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (FormatEnum swigEnum : swigValues)
      if (swigEnum.swigValue == swigValue)
        return swigEnum;
    throw new IllegalArgumentException("No enum " + FormatEnum.class + " with value " + swigValue);
  }

  @SuppressWarnings("unused")
  private FormatEnum() {
    this.swigValue = SwigNext.next++;
  }

  @SuppressWarnings("unused")
  private FormatEnum(int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue+1;
  }

  @SuppressWarnings("unused")
  private FormatEnum(FormatEnum swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue+1;
  }

  private final int swigValue;

  private static class SwigNext {
    private static int next = 0;
  }
}
