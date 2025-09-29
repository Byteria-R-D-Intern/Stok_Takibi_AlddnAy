package com.example.demo.common.error;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;


public class ApiError {

	public Instant timestamp = Instant.now();
	public int status;
	public String error;
	public String message;
	public String path;
	public List<String> details = new ArrayList<>();

	public ApiError() { }

	public ApiError(int status, String error, String message, String path) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}
}


