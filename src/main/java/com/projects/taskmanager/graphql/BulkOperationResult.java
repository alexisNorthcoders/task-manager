package com.projects.taskmanager.graphql;

import java.util.List;
import java.util.ArrayList;

public class BulkOperationResult {
    private boolean success;
    private int updatedCount;
    private List<String> errors;

    public BulkOperationResult() {
        this.errors = new ArrayList<>();
    }

    public BulkOperationResult(boolean success, int updatedCount) {
        this.success = success;
        this.updatedCount = updatedCount;
        this.errors = new ArrayList<>();
    }

    public BulkOperationResult(boolean success, int updatedCount, List<String> errors) {
        this.success = success;
        this.updatedCount = updatedCount;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
}