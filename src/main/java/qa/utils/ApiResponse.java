package qa.utils;

import io.restassured.http.Headers;

public class ApiResponse<T> {

  private final int statusCode;
  private final Headers headers;
  private final T body;

  public ApiResponse(int statusCode, Headers headers, T body) {
    this.statusCode = statusCode;
    this.headers = headers;
    this.body = body;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public Headers getResponseHeaders() {
    return headers;
  }

  public T getResponseBody() {
    return body;
  }
}
