package com.skytracker.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "common-log")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CommonLogsDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String errorMessage;
}