package cat.politecnicllevant.convalidacions.dto;

import lombok.Data;

public @Data class FileUploadDto {
    private String filename;
    private byte[] content; // You can use byte[] to store the file content.

    public FileUploadDto(String filename, byte[] content) {
        this.filename = filename;
        this.content = content;
    }
}
