/*
 * @author mchyzer
 * $Id: GeneratedUtils.java,v 1.1 2008-03-29 10:50:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.webservicesClient.util;


/**
 * util methods for samples
 */
public class GeneratedUtils {
    /**
     * make sure a array is non null.  If null, then return an empty array.
     * Note: this will probably not work for primitive arrays (e.g. int[])
     * @param <T>
     * @param array
     * @return the list or empty list if null
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] nonNull(T[] array) {
        return (array == null) ? ((T[]) new Object[0]) : array;
    }
}
