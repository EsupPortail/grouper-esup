/*
 * @author mchyzer
 * $Id: GenericServletResponseWrapper.java,v 1.2 2009-09-09 15:20:20 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.j2ee;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


/**
 * wrap response so we can customize (e.g. log)
 */
public class GenericServletResponseWrapper extends ServletResponseWrapper implements HttpServletResponse {

  /** keep reference to wrapped response */
  private HttpServletResponse httpServletResponse = null;
  
  /**
   * @param response
   */
  public GenericServletResponseWrapper(HttpServletResponse response) {
    super(response);
    this.httpServletResponse = response;
  }

  /** capture the output */
  ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);

  /** capture the output via writer */
  PrintWriter writer = new PrintWriter(this.baos);
  
  /**
   * @see javax.servlet.ServletResponseWrapper#getOutputStream()
   */
  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return super.getOutputStream();
  }

  /**
   * @see javax.servlet.ServletResponseWrapper#getWriter()
   */
  @Override
  public PrintWriter getWriter() throws IOException {
    return this.writer;
  }

  /**
   * convert the outputstream to a string.  Might want to only call this once...
   * @return the string
   */
  public String resultString() {
    this.writer.flush();
    this.writer.close();
    return this.baos.toString();
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
   */
  public void addCookie(Cookie arg0) {
    this.httpServletResponse.addCookie(arg0);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
   */
  public void addDateHeader(String arg0, long arg1) {
    this.httpServletResponse.addDateHeader(arg0, arg1);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
   */
  public void addHeader(String arg0, String arg1) {
    this.httpServletResponse.addHeader(arg0, arg1);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
   */
  public void addIntHeader(String arg0, int arg1) {
    this.httpServletResponse.addIntHeader(arg0, arg1);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
   */
  public boolean containsHeader(String arg0) {
    return this.httpServletResponse.containsHeader(arg0);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
   */
  public String encodeRedirectURL(String arg0) {
    return this.httpServletResponse.encodeRedirectURL(arg0);
  }

  /**
   * see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
   * @param arg0 
   * @return string
   */
  @SuppressWarnings("deprecation")
  public String encodeRedirectUrl(String arg0) {
    return this.httpServletResponse.encodeRedirectUrl(arg0);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
   */
  public String encodeURL(String arg0) {
    return this.httpServletResponse.encodeURL(arg0);
  }

  /**
   * see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
   * @param arg0 
   * @return string
   */
  @SuppressWarnings("deprecation")
  public String encodeUrl(String arg0) {
    return this.httpServletResponse.encodeUrl(arg0);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#sendError(int)
   */
  public void sendError(int arg0) throws IOException {
    this.httpServletResponse.sendError(arg0);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
   */
  public void sendError(int arg0, String arg1) throws IOException {
    this.httpServletResponse.sendError(arg0, arg1);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
   */
  public void sendRedirect(String arg0) throws IOException {
    this.httpServletResponse.sendRedirect(arg0);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
   */
  public void setDateHeader(String arg0, long arg1) {
    this.httpServletResponse.setDateHeader(arg0, arg1);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
   */
  public void setHeader(String arg0, String arg1) {
    this.httpServletResponse.setHeader(arg0, arg1);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
   */
  public void setIntHeader(String arg0, int arg1) {
    this.httpServletResponse.setIntHeader(arg0, arg1);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#setStatus(int)
   */
  public void setStatus(int arg0) {
    this.httpServletResponse.setStatus(arg0);
  }

  /**
   * see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
   * @param arg0 
   * @param arg1 
   */
  @SuppressWarnings("deprecation")
  public void setStatus(int arg0, String arg1) {
    this.httpServletResponse.setStatus(arg0, arg1);
  }
}
