package com.example.demo.adapter.web.common;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class ResponseCaptureWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream capture = new ByteArrayOutputStream();
    private ServletOutputStream output;
    private PrintWriter writer;

    public ResponseCaptureWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) { throw new IllegalStateException("getWriter() already called"); }
        if (output == null) {
            output = new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException { capture.write(b); }

                @Override
                public boolean isReady() { return true; }

                @Override
                public void setWriteListener(WriteListener writeListener) { }
            };
        }
        return output;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (output != null) { throw new IllegalStateException("getOutputStream() already called"); }
        if (writer == null) { writer = new PrintWriter(capture, true, StandardCharsets.UTF_8); }
        return writer;
    }

    public byte[] getCapturedAsBytes() { return capture.toByteArray(); }
}




