package org.biojava.bio;

import java.util.regex.*;

/**
 * An ec (enzyme classification) number.
 *
 * @for.developer
 * Implementations of this interface should be imutable. This makes them much
 * more usefull as keys in maps.
 * @for.developer
 * it is a good idea to validate that the data being passed in is a sane ec
 * number.
 *
 * @author Matthew Pocock
 * @since 1.4
 */
public interface EcNumber {
  public static final Pattern EC_PATTERN =
    Pattern.compile("((\\d*)|(-))\\.((\\d*)|(-))\\.((\\d*)|(-))\\.((\\d*)|(-))");

  public static final int UNDEFINED = -1;
  public static final int UNCLASSIFIED = 99;

  /**
   * Get the class number associated with the particular level of the ec number.
   *
   * <p>The index can be between 0 and 3 inclusive. 0 correxpons to the top
   * level class, 1 to the sub-class and so on. A return value of UNDEFINED
   * indicates that this field is not populated.</p>
   *
   * @param level  the level in the ec classification to return the number for
   * @return the value at that level
   */
  public int getClassNumber(int level);

  public static class Impl
  implements EcNumber {
    private int[] classes;

    /**
     * Make a new EcNumber.Impl with the data provided.
     */
    public Impl(int mainClass, int subClass, int subSubClass, int group) {
      this.classes = new int[] { mainClass, subClass, subSubClass, group };
    }

    // should be a static factory method - valueOf(String)
    public Impl(String ecString) {
      Matcher matcher = EC_PATTERN.matcher(ecString);
      if(!matcher.matches()) {
        throw new IllegalArgumentException(
          "Can't parse ec string: " + ecString );
      }

      classes = new int[] {
        process(matcher.group(1)),
        process(matcher.group(4)),
        process(matcher.group(7)),
        process(matcher.group(10))
      };
    }

    private int process(String s) {
      if(s.length() > 0) {
        if(s.equals("-")) {
          return UNDEFINED;
        } else {
          return Integer.parseInt(s);
        }
      } else {
        return UNDEFINED;
      }
    }

    public int getClassNumber(int level) {
      return classes[level];
    }

    public String toString() {
      StringBuffer sBuf = new StringBuffer();
      sBuf.append(process(getClassNumber(0)));
      for(int i = 1; i < 4; i++) {
        sBuf.append(".");
        sBuf.append(process(getClassNumber(i)));
      }
      return sBuf.toString();
    }

    private String process(int val) {
      if(val == UNDEFINED) {
        return "";
      } else {
        return Integer.toString(val);
      }
    }

    public boolean equals(Object obj) {
      if(obj instanceof EcNumber) {
        EcNumber that = (EcNumber) obj;

        for(int i = 0; i < 4; i++) {
          if(this.getClassNumber(i) != that.getClassNumber(i)) {
            return false;
          }
        }

        return true;
      }

      return false;
    }

    public int hashCode() {
      return
        getClassNumber(0) * 1000000 +
        getClassNumber(1) * 10000 +
        getClassNumber(2) * 100 +
        getClassNumber(3);
    }
  }
}
