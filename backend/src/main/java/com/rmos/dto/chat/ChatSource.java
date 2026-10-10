package com.rmos.dto.chat;

/**
 * A retrieved RMOS document chunk referenced by an assistant answer.
 */
public class ChatSource {

    private String sourceReference;
    private String documentType;
    private double similarity;

    public ChatSource() {
    }

    public ChatSource(String sourceReference, String documentType, double similarity) {
        this.sourceReference = sourceReference;
        this.documentType = documentType;
        this.similarity = similarity;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
}
