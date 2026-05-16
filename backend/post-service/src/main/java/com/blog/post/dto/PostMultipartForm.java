package com.blog.post.dto;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class PostMultipartForm {
    @RestForm
    public String title;

    @RestForm
    public String content;

    @RestForm
    public String description;

    @RestForm
    public Long authorId;

    @RestForm("image")
    public FileUpload image;
}
