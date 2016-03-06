/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.tierApiAuthzClientExt.org.apache.commons.beanutils;

/**
 * {@link ConvertUtilsBean} implementation that delegates <code>convert()</code>
 * methods to the new {@link ConvertUtilsBean#convert(Object, Class)} method.
 *
 * <p>
 * To configure this implementation for the current context ClassLoader invoke
 * <code>BeanUtilsBean.setInstance(new BeanUtilsBean2());</code>
 * </p>
 *
 * @see BeanUtilsBean2
 * @version $Revision: 552381 $ $Date: 2007-07-02 03:00:17 +0100 (Mon, 02 Jul 2007) $
 * @since 1.8.0
 */
public class ConvertUtilsBean2 extends ConvertUtilsBean {

    /**
     * Delegates to the new {@link ConvertUtilsBean#convert(Object, Class)}
     * method.
     *
     * @param value Value to be converted (may be null)
     * @return The converted String value
     *
     * @see ConvertUtilsBean#convert(String[], Class)
     */
    public String convert(Object value) {
        return (String)convert(value, String.class);
    }

    /**
     * Delegates to the new {@link ConvertUtilsBean#convert(Object, Class)}
     * method.
     *
     * @param value Value to be converted (may be null)
     * @param clazz Java class to be converted to
     * @return The converted value
     *
     * @see ConvertUtilsBean#convert(String[], Class)
     */
    public Object convert(String value, Class clazz) {
        return convert((Object)value, clazz);
    }

    /**
     * Delegates to the new {@link ConvertUtilsBean#convert(Object, Class)}
     * method.
     *
     * @param value Array of values to be converted
     * @param clazz Java array or element class to be converted to
     * @return The converted value
     *
     * @see ConvertUtilsBean#convert(String[], Class)
     */
    public Object convert(String[] value, Class clazz) {
        return convert((Object)value, clazz);
    }

}
