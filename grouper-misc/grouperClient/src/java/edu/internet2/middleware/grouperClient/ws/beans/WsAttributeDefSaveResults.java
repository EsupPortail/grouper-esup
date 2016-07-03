/**
 * Copyright 2016 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouperClient.ws.beans;



/**
 * <pre>
 * results for the attribute defs save call.
 * 
 * result code:
 * code of the result for this attribute def overall
 * SUCCESS: means everything ok
 * ATTRIBUTE_DEF_NOT_FOUND: cant find the attribute def
 * ATTRIBUTE_DEF_DUPLICATE: found multiple attribute defs
 * </pre>
 * @author vsachdeva
 */
public class WsAttributeDefSaveResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * results for each attribute def sent in
   */
  private WsAttributeDefSaveResult[] results;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * results for each attribute def sent in
   * @return the results
   */
  public WsAttributeDefSaveResult[] getResults() {
    return this.results;
  }

  /**
   * results for each attribute def sent in
   * @param results1 the results to set
   */
  public void setResults(WsAttributeDefSaveResult[] results1) {
    this.results = results1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
