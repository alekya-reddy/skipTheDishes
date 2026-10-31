package com.delivery.shifts.config;

import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.Locale;

public class AllureHttpFilter implements Filter {
    @Override
    public Response filter(
            FilterableRequestSpecification request,
            FilterableResponseSpecification responseSpec,
            FilterContext context) {
        Response response = context.next(request, responseSpec);

        String method = request.getMethod();
        String uri = request.getURI();
        String requestBody = request.getBody() == null ? "" : request.getBody().toString();
        String requestDetails = method + " " + uri + "\n\nHeaders:\n"
                + formatHeaders(request.getHeaders().asList().stream()
                        .map(header -> header.getName() + ": " + header.getValue())
                        .toList())
                + "\n\nBody:\n" + requestBody;
        Allure.addAttachment("HTTP Request - " + method + " " + uri, "text/plain", requestDetails);

        String responseBody = response.asString();
        String contentType = response.getContentType();
        String responseMimeType = contentType == null || contentType.toLowerCase(Locale.ROOT).contains("json")
                ? "application/json"
                : "text/plain";
        String responseDetails = "HTTP " + response.statusCode() + " " + response.getStatusLine() + "\n\nHeaders:\n"
                + formatHeaders(response.headers().asList().stream()
                        .map(header -> header.getName() + ": " + header.getValue())
                        .toList())
                + "\n\nBody:\n" + responseBody;
        Allure.addAttachment("HTTP Response - " + response.statusCode() + " " + method + " " + uri,
                responseMimeType, responseDetails);
        ExtentReportManager.logStep(
                "<b>HTTP Request</b><pre>" + escape(requestDetails) + "</pre>"
                        + "<b>HTTP Response</b><pre>" + escape(responseDetails) + "</pre>");

        return response;
    }

    private String formatHeaders(java.util.List<String> headers) {
        return headers.isEmpty() ? "(none)" : String.join("\n", headers);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
